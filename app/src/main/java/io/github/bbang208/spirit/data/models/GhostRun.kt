package io.github.bbang208.spirit.data.models

data class GhostRun(
    val id: String,
    val sessionId: String,
    val trackId: String,
    val lapIndex: Int,
    val lapTimeMs: Long,
    val userId: String,
    val userName: String,
    val telemetryPoints: List<TelemetryPoint>,
    val createdAt: Long
)
