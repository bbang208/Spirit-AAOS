package io.github.bbang208.spirit.data.source.vehicle

import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarPropertyRepositoryImpl @Inject constructor(
    private val carPropertyManager: CarPropertyManager
) : CarPropertyRepository {

    private val registeredCallbacks =
        mutableMapOf<Int, CarPropertyManager.CarPropertyEventCallback>()

    override fun getProperty(propertyId: Int, areaId: Int): CarPropertyValue<*>? {
        return try {
            carPropertyManager.getProperty<Any>(propertyId, areaId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get property: $propertyId")
            null
        }
    }

    override fun registerCallback(
        propertyId: Int,
        sampleRate: Float,
        callback: (CarPropertyValue<*>) -> Unit
    ) {
        val eventCallback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                callback(value)
            }

            override fun onErrorEvent(propertyId: Int, zone: Int) {
                Timber.e("CarProperty error: propertyId=$propertyId, zone=$zone")
            }
        }

        registeredCallbacks[propertyId] = eventCallback
        @Suppress("DEPRECATION")
        carPropertyManager.registerCallback(eventCallback, propertyId, sampleRate)
    }

    override fun unregisterCallback(propertyId: Int) {
        registeredCallbacks.remove(propertyId)?.let { callback ->
            @Suppress("DEPRECATION")
            carPropertyManager.unregisterCallback(callback)
        }
    }

    override fun unregisterAllCallbacks() {
        registeredCallbacks.forEach { (_, callback) ->
            @Suppress("DEPRECATION")
            carPropertyManager.unregisterCallback(callback)
        }
        registeredCallbacks.clear()
    }
}
