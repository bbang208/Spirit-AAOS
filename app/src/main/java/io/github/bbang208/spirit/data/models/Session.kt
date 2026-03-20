package io.github.bbang208.spirit.data.models

data class Session(
    val id: String,
    val trackId: String,
    val startTime: Long,
    val endTime: Long?,
    val totalLaps: Int,
    val bestLapTimeMs: Long?,
    val status: SessionStatus
)

enum class SessionStatus {
    ACTIVE, COMPLETED, ABANDONED
}
