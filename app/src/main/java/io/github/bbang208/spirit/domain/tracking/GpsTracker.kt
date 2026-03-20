package io.github.bbang208.spirit.domain.tracking

import io.github.bbang208.spirit.data.models.GpsPoint
import io.github.bbang208.spirit.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

enum class TrackingState {
    IDLE, TRACKING
}

@Singleton
class GpsTracker @Inject constructor(
    private val locationProvider: LocationProvider
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(TrackingState.IDLE)
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private val _currentLocation = MutableStateFlow<GpsPoint?>(null)
    val currentLocation: StateFlow<GpsPoint?> = _currentLocation.asStateFlow()

    private val buffer = ArrayDeque<GpsPoint>(Constants.GPS_BUFFER_SIZE)
    private val bufferMutex = Mutex()

    private var collectionJob: Job? = null

    fun startTracking() {
        if (_state.value == TrackingState.TRACKING) return

        _state.value = TrackingState.TRACKING

        collectionJob = scope.launch {
            locationProvider.startTracking().collect { point ->
                if (point.accuracy > Constants.GPS_MIN_ACCURACY_METERS) return@collect

                _currentLocation.value = point

                bufferMutex.withLock {
                    if (buffer.size >= Constants.GPS_BUFFER_SIZE) {
                        buffer.removeFirst()
                    }
                    buffer.addLast(point)
                }
            }
        }
    }

    fun stopTracking() {
        collectionJob?.cancel()
        collectionJob = null
        locationProvider.stopTracking()
        _state.value = TrackingState.IDLE
        _currentLocation.value = null
    }

    suspend fun getAllBufferedPoints(): List<GpsPoint> {
        return bufferMutex.withLock { buffer.toList() }
    }

    suspend fun getRecentPoints(count: Int): List<GpsPoint> {
        return bufferMutex.withLock {
            buffer.takeLast(count)
        }
    }

    suspend fun clearBuffer() {
        bufferMutex.withLock { buffer.clear() }
    }
}
