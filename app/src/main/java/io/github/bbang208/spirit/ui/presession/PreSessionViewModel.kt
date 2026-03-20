package io.github.bbang208.spirit.ui.presession

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bbang208.spirit.data.source.local.db.dao.SessionDao
import io.github.bbang208.spirit.data.source.local.db.dao.TrackDao
import io.github.bbang208.spirit.data.source.local.db.entity.SessionEntity
import io.github.bbang208.spirit.data.source.local.db.entity.TrackEntity
import io.github.bbang208.spirit.util.Event
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PreSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackDao: TrackDao,
    private val sessionDao: SessionDao
) : ViewModel() {

    val trackId: String = savedStateHandle["trackId"] ?: ""

    val track: LiveData<TrackEntity?> = trackDao.getTrackById(trackId)

    private val _sectorCount = MutableLiveData(3)
    val sectorCount: LiveData<Int> = _sectorCount

    private val _ghostEnabled = MutableLiveData(false)
    val ghostEnabled: LiveData<Boolean> = _ghostEnabled

    private val _sessionCreated = MutableLiveData<Event<String>>()
    val sessionCreated: LiveData<Event<String>> = _sessionCreated

    fun incrementSectors() {
        val current = _sectorCount.value ?: 3
        if (current < 9) _sectorCount.value = current + 1
    }

    fun decrementSectors() {
        val current = _sectorCount.value ?: 3
        if (current > 2) _sectorCount.value = current - 1
    }

    fun setGhostEnabled(enabled: Boolean) {
        _ghostEnabled.value = enabled
    }

    fun createSession() {
        viewModelScope.launch {
            val sessionId = UUID.randomUUID().toString()
            val session = SessionEntity(
                id = sessionId,
                trackId = trackId,
                startTime = System.currentTimeMillis(),
                endTime = null,
                totalLaps = 0,
                bestLapTimeMs = null,
                status = "ACTIVE",
                isUploaded = false
            )
            sessionDao.insert(session)
            _sessionCreated.postValue(Event(sessionId))
        }
    }
}
