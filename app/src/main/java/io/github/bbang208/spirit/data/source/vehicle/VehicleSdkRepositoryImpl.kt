package io.github.bbang208.spirit.data.source.vehicle

import ai.pleos.playground.vehicle.Vehicle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleSdkRepositoryImpl @Inject constructor(
    private val vehicle: Vehicle
) : VehicleSdkRepository {

    override fun getVehicle(): Vehicle = vehicle

    override fun initialize() {
        vehicle.initialize()
    }

    override fun release() {
        vehicle.release()
    }
}
