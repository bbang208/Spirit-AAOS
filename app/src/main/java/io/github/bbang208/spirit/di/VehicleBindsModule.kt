package io.github.bbang208.spirit.di

import io.github.bbang208.spirit.data.source.vehicle.CarPropertyRepository
import io.github.bbang208.spirit.data.source.vehicle.CarPropertyRepositoryImpl
import io.github.bbang208.spirit.data.source.vehicle.VehicleSdkRepository
import io.github.bbang208.spirit.data.source.vehicle.VehicleSdkRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface VehicleBindsModule {

    @Binds
    fun bindCarPropertyRepository(impl: CarPropertyRepositoryImpl): CarPropertyRepository

    @Binds
    fun bindVehicleSdkRepository(impl: VehicleSdkRepositoryImpl): VehicleSdkRepository
}
