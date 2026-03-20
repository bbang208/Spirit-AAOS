package io.github.bbang208.spirit.domain.tracking

import com.google.gson.Gson
import io.github.bbang208.spirit.data.models.GpsPoint
import io.github.bbang208.spirit.data.models.Sector
import io.github.bbang208.spirit.data.source.local.db.dao.TrackDao
import io.github.bbang208.spirit.data.source.local.db.entity.TrackEntity
import io.github.bbang208.spirit.util.Constants
import io.github.bbang208.spirit.util.GeoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class RecorderState {
    IDLE, RECORDING, COMPLETED
}

@Singleton
class TrackRecorder @Inject constructor(
    private val gpsTracker: GpsTracker,
    private val trackDao: TrackDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val gson = Gson()

    private val _state = MutableStateFlow(RecorderState.IDLE)
    val state: StateFlow<RecorderState> = _state.asStateFlow()

    private val _recordedPoints = MutableStateFlow<List<GpsPoint>>(emptyList())
    val recordedPoints: StateFlow<List<GpsPoint>> = _recordedPoints.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()

    private val _distanceToStart = MutableStateFlow(0.0)
    val distanceToStart: StateFlow<Double> = _distanceToStart.asStateFlow()

    private val points = mutableListOf<GpsPoint>()
    private var startPoint: GpsPoint? = null
    private var monitorJob: Job? = null

    fun startRecording() {
        if (_state.value != RecorderState.IDLE) return

        points.clear()
        startPoint = null
        _recordedPoints.value = emptyList()
        _totalDistance.value = 0.0
        _state.value = RecorderState.RECORDING

        gpsTracker.startTracking()

        monitorJob = scope.launch {
            gpsTracker.currentLocation.collect { point ->
                if (point == null) return@collect
                if (_state.value != RecorderState.RECORDING) return@collect

                if (startPoint == null) {
                    startPoint = point
                }

                points.add(point)
                _recordedPoints.value = points.toList()

                // Update total distance
                if (points.size >= 2) {
                    val prev = points[points.size - 2]
                    _totalDistance.value += GeoUtils.haversineDistance(
                        prev.latitude, prev.longitude,
                        point.latitude, point.longitude
                    )
                }

                // Update distance to start for loop closure UI
                val start = startPoint
                if (start != null && points.size >= 2) {
                    _distanceToStart.value = GeoUtils.haversineDistance(
                        start.latitude, start.longitude,
                        point.latitude, point.longitude
                    )
                }

                // Loop detection: after 100 points, check if close to start
                if (points.size >= 100 && start != null) {
                    val dist = _distanceToStart.value
                    val bearingDiff = GeoUtils.bearingDifference(start.bearing, point.bearing)

                    if (dist <= Constants.TRACK_CLOSE_DISTANCE_METERS && bearingDiff < Constants.BEARING_TOLERANCE_DEGREES) {
                        _state.value = RecorderState.COMPLETED
                        gpsTracker.stopTracking()
                    }
                }
            }
        }
    }

    fun buildTrack(name: String, sectorCount: Int): TrackEntity {
        val start = startPoint ?: throw IllegalStateException("No start point recorded")
        val allPoints = points.toList()

        // Simplify outline
        val outline = GeoUtils.douglasPeucker(allPoints, 5.0)

        // Calculate total length from outline
        var totalLength = 0.0
        for (i in 1 until outline.size) {
            totalLength += GeoUtils.haversineDistance(
                outline[i - 1].latitude, outline[i - 1].longitude,
                outline[i].latitude, outline[i].longitude
            )
        }

        // Build sectors by dividing distance equally
        val sectors = buildSectors(allPoints, sectorCount, totalLength)

        val trackId = UUID.randomUUID().toString()
        return TrackEntity(
            id = trackId,
            name = name,
            description = "",
            address = "",
            grade = "",
            startLineLat = start.latitude,
            startLineLng = start.longitude,
            startLineBearing = start.bearing,
            lengthMeters = totalLength.toFloat(),
            outlineJson = gson.toJson(outline),
            sectorsJson = gson.toJson(sectors),
            isLocal = true,
            isPreset = false,
            remoteId = null,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun buildSectors(
        allPoints: List<GpsPoint>,
        sectorCount: Int,
        totalLength: Double
    ): List<Sector> {
        if (sectorCount <= 0 || allPoints.size < 2) return emptyList()

        val sectorLength = totalLength / sectorCount
        val sectors = mutableListOf<Sector>()
        var accumulated = 0.0

        for (i in 1 until allPoints.size) {
            val segDist = GeoUtils.haversineDistance(
                allPoints[i - 1].latitude, allPoints[i - 1].longitude,
                allPoints[i].latitude, allPoints[i].longitude
            )
            accumulated += segDist

            val nextSectorIndex = sectors.size + 1
            if (nextSectorIndex < sectorCount && accumulated >= sectorLength * nextSectorIndex) {
                sectors.add(
                    Sector(
                        index = nextSectorIndex,
                        gateLat = allPoints[i].latitude,
                        gateLng = allPoints[i].longitude,
                        gateBearing = allPoints[i].bearing
                    )
                )
            }
        }

        return sectors
    }

    suspend fun saveTrack(track: TrackEntity) {
        trackDao.insert(track)
    }

    fun cancelRecording() {
        monitorJob?.cancel()
        monitorJob = null
        gpsTracker.stopTracking()
        points.clear()
        startPoint = null
        _recordedPoints.value = emptyList()
        _totalDistance.value = 0.0
        _distanceToStart.value = 0.0
        _state.value = RecorderState.IDLE
    }

    fun reset() {
        monitorJob?.cancel()
        monitorJob = null
        points.clear()
        startPoint = null
        _recordedPoints.value = emptyList()
        _totalDistance.value = 0.0
        _distanceToStart.value = 0.0
        _state.value = RecorderState.IDLE
    }
}
