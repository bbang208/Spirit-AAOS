package io.github.bbang208.spirit.domain.timing

data class GateEndpoints(
    val lat1: Double,
    val lng1: Double,
    val lat2: Double,
    val lng2: Double
)

data class LapCrossing(
    val crossingTimeMs: Long,
    val lapIndex: Int
)

data class SectorCrossing(
    val crossingTimeMs: Long,
    val sectorIndex: Int
)

data class GForceData(
    val lateralG: Float,
    val longitudinalG: Float,
    val timestamp: Long
)

enum class TimingState {
    IDLE, WAITING_FOR_START, RUNNING
}

enum class SectorStatus {
    PENDING, CURRENT, COMPLETED, PERSONAL_BEST
}

data class SectorState(
    val index: Int,
    val status: SectorStatus,
    val timeMs: Long?
)
