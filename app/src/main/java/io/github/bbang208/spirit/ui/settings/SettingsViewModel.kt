package io.github.bbang208.spirit.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.bbang208.spirit.di.ConfigSharedPreference
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ConfigSharedPreference private val configPrefs: SharedPreferences
) : ViewModel() {

    companion object {
        private const val KEY_SPEED_UNIT = "speed_unit"
        const val UNIT_KMH = "kmh"
        const val UNIT_MPH = "mph"
    }

    private val _speedUnit = MutableLiveData(
        configPrefs.getString(KEY_SPEED_UNIT, UNIT_KMH) ?: UNIT_KMH
    )
    val speedUnit: LiveData<String> = _speedUnit

    fun setSpeedUnit(unit: String) {
        _speedUnit.value = unit
        configPrefs.edit().putString(KEY_SPEED_UNIT, unit).apply()
    }
}
