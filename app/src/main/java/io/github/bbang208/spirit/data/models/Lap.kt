package io.github.bbang208.spirit.data.models

data class Lap(
    val id: String,
    val sessionId: String,
    val lapIndex: Int,
    val lapTimeMs: Long,
    val isPersonalBest: Boolean,
    val deltaToBestMs: Long?,
    val sectors: List<LapSector>,
    val gpsPoints: List<GpsPoint>,
    val telemetryPoints: List<TelemetryPoint>
)
