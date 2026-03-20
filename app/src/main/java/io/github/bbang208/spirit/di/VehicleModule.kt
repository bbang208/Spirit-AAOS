package io.github.bbang208.spirit.di

import ai.pleos.playground.vehicle.Vehicle
import android.car.Car
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleModule {

    @Provides
    @Singleton
    fun provideVehicle(
        @ApplicationContext context: Context
    ): Vehicle = Vehicle(context)

    @Provides
    @Singleton
    fun provideCar(
        @ApplicationContext context: Context
    ): Car = Car.createCar(context)

    @Provides
    @Singleton
    fun provideCarPropertyManager(
        car: Car
    ): CarPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
}
