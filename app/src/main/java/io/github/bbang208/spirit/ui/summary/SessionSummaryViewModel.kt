package io.github.bbang208.spirit.ui.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bbang208.spirit.data.source.local.db.dao.LapDao
import io.github.bbang208.spirit.data.source.local.db.dao.SessionDao
import io.github.bbang208.spirit.data.source.local.db.dao.TrackDao
import io.github.bbang208.spirit.data.source.local.db.entity.LapEntity
import io.github.bbang208.spirit.data.source.local.db.entity.SessionEntity
import io.github.bbang208.spirit.data.source.local.db.entity.TrackEntity
import io.github.bbang208.spirit.util.AbsentLiveData
import javax.inject.Inject

@HiltViewModel
class SessionSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionDao: SessionDao,
    private val lapDao: LapDao,
    private val trackDao: TrackDao
) : ViewModel() {

    private val sessionId: String = savedStateHandle["sessionId"] ?: ""

    val session: LiveData<SessionEntity?> = sessionDao.getSessionById(sessionId)
    val laps: LiveData<List<LapEntity>> = lapDao.getLapsBySession(sessionId)

    val track: LiveData<TrackEntity?> = session.switchMap { s ->
        s?.let { trackDao.getTrackById(it.trackId) } ?: AbsentLiveData.create()
    }
}
