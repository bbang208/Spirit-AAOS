package io.github.bbang208.spirit.domain.tracking

import io.github.bbang208.spirit.data.models.GpsPoint
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    fun startTracking(): Flow<GpsPoint>
    fun stopTracking()
    fun isTracking(): Boolean
}
