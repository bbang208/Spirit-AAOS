package io.github.bbang208.spirit.domain.timing

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.bbang208.spirit.data.models.Sector
import io.github.bbang208.spirit.data.source.local.db.dao.LapDao
import io.github.bbang208.spirit.data.source.local.db.dao.SessionDao
import io.github.bbang208.spirit.data.source.local.db.dao.TrackDao
import io.github.bbang208.spirit.data.source.local.db.entity.LapEntity
import io.github.bbang208.spirit.data.source.local.db.entity.LapSectorEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LapTimer @Inject constructor(
    private val lapDetector: LapDetector,
    private val sectorSplitter: SectorSplitter,
    private val trackDao: TrackDao,
    private val sessionDao: SessionDao,
    private val lapDao: LapDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val gson = Gson()

    // State outputs
    private val _state = MutableStateFlow(TimingState.IDLE)
    val state: StateFlow<TimingState> = _state.asStateFlow()

    private val _currentLapNumber = MutableStateFlow(0)
    val currentLapNumber: StateFlow<Int> = _currentLapNumber.asStateFlow()

    private val _currentLapTimeMs = MutableStateFlow(0L)
    val currentLapTimeMs: StateFlow<Long> = _currentLapTimeMs.asStateFlow()

    private val _sessionElapsedMs = MutableStateFlow(0L)
    val sessionElapsedMs: StateFlow<Long> = _sessionElapsedMs.asStateFlow()

    private val _bestLapTimeMs = MutableStateFlow<Long?>(null)
    val bestLapTimeMs: StateFlow<Long?> = _bestLapTimeMs.asStateFlow()

    private val _deltaTimeMs = MutableStateFlow(0L)
    val deltaTimeMs: StateFlow<Long> = _deltaTimeMs.asStateFlow()

    private val _currentSectorIndex = MutableStateFlow(0)
    val currentSectorIndex: StateFlow<Int> = _currentSectorIndex.asStateFlow()

    private val _sectorStates = MutableStateFlow<List<SectorState>>(emptyList())
    val sectorStates: StateFlow<List<SectorState>> = _sectorStates.asStateFlow()

    private val _sectorCount = MutableStateFlow(0)
    val sectorCount: StateFlow<Int> = _sectorCount.asStateFlow()

    // Internal state
    private var sessionId: String = ""
    private var trackId: String = ""
    private var lapStartTimeMs: Long = 0L
    private var sessionStartTimeMs: Long = 0L
    private var timerJob: Job? = null
    private var lapDetectorJob: Job? = null
    private var sectorDetectorJob: Job? = null
    private var sectorTimings: MutableList<Long> = mutableListOf()
    private var bestSectorTimings: MutableList<Long?> = mutableListOf()

    fun startSession(sessionId: String, trackId: String) {
        this.sessionId = sessionId
        this.trackId = trackId

        scope.launch {
            val track = trackDao.getTrackByIdSync(trackId) ?: return@launch

            // Configure lap detector
            lapDetector.configure(
                track.startLineLat,
                track.startLineLng,
                track.startLineBearing
            )

            // Configure sector splitter
            val sectorType = object : TypeToken<List<Sector>>() {}.type
            val sectors: List<Sector> = gson.fromJson(track.sectorsJson, sectorType) ?: emptyList()
            sectorSplitter.configure(sectors)
            _sectorCount.value = sectors.size

            // Initialize best sector timings
            bestSectorTimings = MutableList(sectors.size) { null }

            // Initialize sector states
            _sectorStates.value = List(sectors.size) { i ->
                SectorState(i, SectorStatus.PENDING, null)
            }

            _state.value = TimingState.WAITING_FOR_START

            // Start listening for lap crossings
            lapDetectorJob = scope.launch {
                lapDetector.observeCrossings().collect { crossing ->
                    handleLapCrossing(crossing)
                }
            }

            // Start listening for sector crossings
            sectorDetectorJob = scope.launch {
                sectorSplitter.observeCrossings().collect { crossing ->
                    handleSectorCrossing(crossing)
                }
            }
        }
    }

    private suspend fun handleLapCrossing(crossing: LapCrossing) {
        if (crossing.lapIndex == 0) {
            // First crossing — start timing
            lapStartTimeMs = crossing.crossingTimeMs
            sessionStartTimeMs = crossing.crossingTimeMs
            _currentLapNumber.value = 1
            _state.value = TimingState.RUNNING
            sectorTimings.clear()

            // Reset sector states to first sector current
            updateSectorStatesForNewLap()

            // Start timer tick
            startTimerTick()
        } else {
            // Subsequent crossing — lap completed
            val lapTimeMs = crossing.crossingTimeMs - lapStartTimeMs

            // Save completed lap
            saveLap(crossing.lapIndex - 1, lapTimeMs)

            // Update best lap
            val currentBest = _bestLapTimeMs.value
            if (currentBest == null || lapTimeMs < currentBest) {
                _bestLapTimeMs.value = lapTimeMs
                _deltaTimeMs.value = 0L
            } else {
                _deltaTimeMs.value = lapTimeMs - currentBest
            }

            // Start new lap
            lapStartTimeMs = crossing.crossingTimeMs
            _currentLapNumber.value = crossing.lapIndex + 1
            _currentLapTimeMs.value = 0L
            sectorTimings.clear()

            // Reset sectors
            sectorSplitter.resetToSector(0)
            _currentSectorIndex.value = 0
            updateSectorStatesForNewLap()
        }
    }

    private fun handleSectorCrossing(crossing: SectorCrossing) {
        val sectorTimeMs = if (sectorTimings.isEmpty()) {
            crossing.crossingTimeMs - lapStartTimeMs
        } else {
            crossing.crossingTimeMs - lapStartTimeMs - sectorTimings.sum()
        }

        sectorTimings.add(sectorTimeMs)
        _currentSectorIndex.value = crossing.sectorIndex + 1

        // Update sector states
        val states = _sectorStates.value.toMutableList()
        val bestTime = bestSectorTimings.getOrNull(crossing.sectorIndex)
        val isPersonalBest = bestTime == null || sectorTimeMs < bestTime

        if (isPersonalBest) {
            bestSectorTimings[crossing.sectorIndex] = sectorTimeMs
        }

        states[crossing.sectorIndex] = SectorState(
            index = crossing.sectorIndex,
            status = if (isPersonalBest) SectorStatus.PERSONAL_BEST else SectorStatus.COMPLETED,
            timeMs = sectorTimeMs
        )

        // Mark next sector as current
        val nextIndex = crossing.sectorIndex + 1
        if (nextIndex < states.size) {
            states[nextIndex] = SectorState(nextIndex, SectorStatus.CURRENT, null)
        }

        _sectorStates.value = states
    }

    private fun updateSectorStatesForNewLap() {
        val count = _sectorCount.value
        if (count == 0) return

        _sectorStates.value = List(count) { i ->
            if (i == 0) SectorState(i, SectorStatus.CURRENT, null)
            else SectorState(i, SectorStatus.PENDING, null)
        }
    }

    private fun startTimerTick() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _currentLapTimeMs.value = now - lapStartTimeMs
                _sessionElapsedMs.value = now - sessionStartTimeMs
                delay(16L)
            }
        }
    }

    private suspend fun saveLap(lapIndex: Int, lapTimeMs: Long) {
        val bestLap = _bestLapTimeMs.value
        val isPersonalBest = bestLap == null || lapTimeMs <= bestLap
        val deltaToBest = if (bestLap != null) lapTimeMs - bestLap else null

        val lapId = UUID.randomUUID().toString()
        val lapEntity = LapEntity(
            id = lapId,
            sessionId = sessionId,
            lapIndex = lapIndex,
            lapTimeMs = lapTimeMs,
            isPersonalBest = isPersonalBest,
            deltaToBest = deltaToBest
        )
        lapDao.insertLap(lapEntity)

        // Save sector timings
        if (sectorTimings.isNotEmpty()) {
            val sectorEntities = sectorTimings.mapIndexed { i, timeMs ->
                val bestSector = bestSectorTimings.getOrNull(i)
                val isSectorBest = bestSector == null || timeMs <= bestSector
                val sectorDelta = bestSector?.let { timeMs - it }

                LapSectorEntity(
                    lapId = lapId,
                    sectorIndex = i,
                    timeMs = timeMs,
                    isPersonalBest = isSectorBest,
                    deltaMs = sectorDelta
                )
            }
            lapDao.insertLapSectors(sectorEntities)
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        lapDetectorJob?.cancel()
        sectorDetectorJob?.cancel()

        scope.launch {
            val session = sessionDao.getSessionByIdSync(sessionId)
            if (session != null) {
                sessionDao.update(
                    session.copy(
                        endTime = System.currentTimeMillis(),
                        totalLaps = (_currentLapNumber.value - 1).coerceAtLeast(0),
                        bestLapTimeMs = _bestLapTimeMs.value,
                        status = "COMPLETED"
                    )
                )
            }
        }

        // Reset all state
        _state.value = TimingState.IDLE
        _currentLapNumber.value = 0
        _currentLapTimeMs.value = 0L
        _sessionElapsedMs.value = 0L
        _bestLapTimeMs.value = null
        _deltaTimeMs.value = 0L
        _currentSectorIndex.value = 0
        _sectorStates.value = emptyList()
        _sectorCount.value = 0
        sectorTimings.clear()
        lapDetector.reset()
    }
}
