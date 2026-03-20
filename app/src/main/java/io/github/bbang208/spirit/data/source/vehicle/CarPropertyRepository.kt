package io.github.bbang208.spirit.data.source.vehicle

import android.car.hardware.CarPropertyValue

interface CarPropertyRepository {

    fun getProperty(propertyId: Int, areaId: Int): CarPropertyValue<*>?

    fun registerCallback(
        propertyId: Int,
        sampleRate: Float,
        callback: (CarPropertyValue<*>) -> Unit
    )

    fun unregisterCallback(propertyId: Int)

    fun unregisterAllCallbacks()
}
