package io.github.bbang208.spirit.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bbang208.spirit.data.source.local.db.dao.SessionDao
import io.github.bbang208.spirit.data.source.local.db.entity.SessionWithTrack
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionDao: SessionDao
) : ViewModel() {

    val recentSessions: LiveData<List<SessionWithTrack>> =
        sessionDao.getRecentSessionsWithTrack(4)

    val hasNoSessions: LiveData<Boolean> = recentSessions.map { it.isEmpty() }
}
