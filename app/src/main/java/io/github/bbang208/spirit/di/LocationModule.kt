package io.github.bbang208.spirit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.bbang208.spirit.domain.tracking.LocationProvider
import io.github.bbang208.spirit.domain.tracking.MockLocationProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationProvider(): LocationProvider = MockLocationProvider()
}
