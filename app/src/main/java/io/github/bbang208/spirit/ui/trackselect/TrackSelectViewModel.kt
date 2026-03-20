package io.github.bbang208.spirit.ui.trackselect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bbang208.spirit.data.source.local.db.dao.TrackDao
import io.github.bbang208.spirit.data.source.local.db.entity.TrackEntity
import javax.inject.Inject

@HiltViewModel
class TrackSelectViewModel @Inject constructor(
    private val trackDao: TrackDao
) : ViewModel() {

    val localTracks: LiveData<List<TrackEntity>> = trackDao.getLocalTracks()
    val presetTracks: LiveData<List<TrackEntity>> = trackDao.getPresetTracks()
    val allTracks: LiveData<List<TrackEntity>> = trackDao.getAllTracks()

    private val _selectedTab = MutableLiveData<Int>().apply { value = 0 }
    val selectedTab: LiveData<Int> = _selectedTab

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    val displayedTracks: LiveData<List<TrackEntity>> = selectedTab.switchMap { tab ->
        when (tab) {
            0 -> presetTracks
            1 -> localTracks
            else -> allTracks
        }
    }
}
