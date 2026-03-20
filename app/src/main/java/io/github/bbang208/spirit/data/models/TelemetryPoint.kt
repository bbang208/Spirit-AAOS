package io.github.bbang208.spirit.data.models

data class TelemetryPoint(
    val timestamp: Long,
    val speedKmh: Float,
    val lateralG: Float,
    val longitudinalG: Float,
    val latitude: Double,
    val longitude: Double
)
