package io.github.bbang208.spirit.data.models

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Long,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float
)
