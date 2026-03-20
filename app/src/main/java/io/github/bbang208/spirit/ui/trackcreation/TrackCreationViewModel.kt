package io.github.bbang208.spirit.ui.trackcreation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bbang208.spirit.data.models.GpsPoint
import io.github.bbang208.spirit.domain.tracking.RecorderState
import io.github.bbang208.spirit.domain.tracking.TrackRecorder
import io.github.bbang208.spirit.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackCreationViewModel @Inject constructor(
    private val trackRecorder: TrackRecorder
) : ViewModel() {

    val recorderState: LiveData<RecorderState> = trackRecorder.state.asLiveData()
    val livePoints: LiveData<List<GpsPoint>> = trackRecorder.recordedPoints.asLiveData()
    val totalDistance: LiveData<Double> = trackRecorder.totalDistance.asLiveData()
    val distanceToStart: LiveData<Double> = trackRecorder.distanceToStart.asLiveData()
    val gpsPointCount: LiveData<Int> = livePoints.map { it.size }

    val trackName = MutableLiveData("")

    private val _sectorCount = MutableLiveData(3)
    val sectorCount: LiveData<Int> = _sectorCount

    private val _trackSaved = MutableLiveData<Event<String>>()
    val trackSaved: LiveData<Event<String>> = _trackSaved

    fun startRecording() {
        trackRecorder.startRecording()
    }

    fun cancelRecording() {
        trackRecorder.cancelRecording()
    }

    fun incrementSectors() {
        val current = _sectorCount.value ?: 3
        if (current < 9) _sectorCount.value = current + 1
    }

    fun decrementSectors() {
        val current = _sectorCount.value ?: 3
        if (current > 2) _sectorCount.value = current - 1
    }

    fun saveTrack() {
        val name = trackName.value?.takeIf { it.isNotBlank() } ?: return
        val sectors = _sectorCount.value ?: 3

        viewModelScope.launch {
            val track = trackRecorder.buildTrack(name, sectors)
            trackRecorder.saveTrack(track)
            trackRecorder.reset()
            _trackSaved.postValue(Event(track.id))
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (recorderState.value == RecorderState.RECORDING) {
            trackRecorder.cancelRecording()
        }
    }
}
