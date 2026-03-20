package io.github.bbang208.spirit.data.source.vehicle

import ai.pleos.playground.vehicle.Vehicle

interface VehicleSdkRepository {

    fun getVehicle(): Vehicle

    fun initialize()

    fun release()
}
