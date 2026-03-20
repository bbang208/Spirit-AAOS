package io.github.bbang208.spirit.data.source.vehicle

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrivingStateRepository @Inject constructor(
    private val car: Car
) {
    private val _isDriving = MutableLiveData(false)
    val isDriving: LiveData<Boolean> = _isDriving

    private var restrictionsManager: CarUxRestrictionsManager? = null
    private var listener: CarUxRestrictionsManager.OnUxRestrictionsChangedListener? = null

    fun startMonitoring() {
        try {
            restrictionsManager = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE)
                as? CarUxRestrictionsManager

            listener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { restrictions ->
                val driving = restrictions.activeRestrictions != CarUxRestrictions.UX_RESTRICTIONS_BASELINE
                _isDriving.postValue(driving)
                Timber.d("Driving state changed: driving=$driving, restrictions=${restrictions.activeRestrictions}")
            }

            restrictionsManager?.registerListener(listener!!)

            // Check initial state
            restrictionsManager?.currentCarUxRestrictions?.let { restrictions ->
                val driving = restrictions.activeRestrictions != CarUxRestrictions.UX_RESTRICTIONS_BASELINE
                _isDriving.postValue(driving)
            }

            Timber.d("DrivingStateRepository: monitoring started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start driving state monitoring")
            _isDriving.postValue(false)
        }
    }

    fun stopMonitoring() {
        restrictionsManager?.unregisterListener()
        restrictionsManager = null
        listener = null
        Timber.d("DrivingStateRepository: monitoring stopped")
    }
}
