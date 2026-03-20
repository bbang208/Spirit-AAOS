package io.github.bbang208.spirit.ui.livetiming

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.data.source.vehicle.SpeedDataSource
import io.github.bbang208.spirit.domain.timing.GForceCalculator
import io.github.bbang208.spirit.domain.timing.LapTimer
import io.github.bbang208.spirit.domain.timing.SectorState
import io.github.bbang208.spirit.domain.timing.TimingState
import io.github.bbang208.spirit.util.TimeFormatter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class LiveTimingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lapTimer: LapTimer,
    private val gForceCalculator: GForceCalculator,
    private val speedDataSource: SpeedDataSource
) : ViewModel() {

    val trackId: String = savedStateHandle["trackId"] ?: ""
    val sessionId: String = savedStateHandle["sessionId"] ?: ""
    val ghostRunId: String? = savedStateHandle["ghostRunId"]

    // Timing state
    val timingState: LiveData<TimingState> = lapTimer.state.asLiveData()

    val currentLapNumber: LiveData<Int> = lapTimer.currentLapNumber.asLiveData()

    val currentLapTimeMs: LiveData<Long> = lapTimer.currentLapTimeMs.asLiveData()

    val sessionElapsedMs: LiveData<Long> = lapTimer.sessionElapsedMs.asLiveData()

    val deltaTimeMs: LiveData<Long> = lapTimer.deltaTimeMs.asLiveData()

    val sectorStates: LiveData<List<SectorState>> = lapTimer.sectorStates.asLiveData()

    // Formatted strings
    val formattedLapLabel: LiveData<String> = currentLapNumber.map { lap ->
        if (lap > 0) "LAP $lap" else "LAP --"
    }

    val formattedSessionTime: LiveData<String> = sessionElapsedMs.map { ms ->
        TimeFormatter.formatLapTime(ms)
    }

    val formattedLapTime: LiveData<String> = currentLapTimeMs.map { ms ->
        TimeFormatter.formatLapTime(ms)
    }

    val formattedDelta: LiveData<String> = deltaTimeMs.map { ms ->
        if (ms == 0L) "" else TimeFormatter.formatDelta(ms)
    }

    val deltaColorRes: LiveData<Int> = deltaTimeMs.map { ms ->
        when {
            ms < 0 -> R.color.switch_on          // green — faster
            ms > 0 -> R.color.call_end_normal     // red — slower
            else -> R.color.basic_300
        }
    }

    // Speed from vehicle
    val formattedSpeed: LiveData<String> = speedDataSource.observeSpeedKmh()
        .map { speed -> "%.0f".format(speed) }
        .asLiveData()

    // G-Force
    private val gForceData = gForceCalculator.observeGForce().asLiveData()

    val lateralG: LiveData<Float> = gForceData.map { it.lateralG }
    val longitudinalG: LiveData<Float> = gForceData.map { it.longitudinalG }

    // Speed as float for binding
    val speedKmh: LiveData<Float> = speedDataSource.observeSpeedKmh().asLiveData()

    init {
        if (sessionId.isNotEmpty() && trackId.isNotEmpty()) {
            lapTimer.startSession(sessionId, trackId)
        }
    }

    fun stopSession() {
        lapTimer.stopSession()
    }

    override fun onCleared() {
        super.onCleared()
        lapTimer.stopSession()
    }
}
