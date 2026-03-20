package io.github.bbang208.spirit.data.source.vehicle

import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedDataSource @Inject constructor(
    private val carPropertyRepository: CarPropertyRepository
) {
    companion object {
        private const val MS_TO_KMH = 3.6f
        private const val SAMPLE_RATE = 10f // 10Hz
    }

    fun observeSpeedKmh(): Flow<Float> = callbackFlow {
        val callback: (CarPropertyValue<*>) -> Unit = { value ->
            val speedMs = (value.value as? Float) ?: 0f
            val speedKmh = speedMs * MS_TO_KMH
            trySend(speedKmh)
        }

        carPropertyRepository.registerCallback(
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            SAMPLE_RATE,
            callback
        )
        Timber.d("SpeedDataSource: registered speed callback")

        awaitClose {
            carPropertyRepository.unregisterCallback(VehiclePropertyIds.PERF_VEHICLE_SPEED)
            Timber.d("SpeedDataSource: unregistered speed callback")
        }
    }

    fun getCurrentSpeedKmh(): Float {
        val value = carPropertyRepository.getProperty(
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            0
        )
        val speedMs = (value?.value as? Float) ?: 0f
        return speedMs * MS_TO_KMH
    }
}
