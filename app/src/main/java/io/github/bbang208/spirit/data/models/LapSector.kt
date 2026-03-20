package io.github.bbang208.spirit.data.models

data class LapSector(
    val sectorIndex: Int,
    val timeMs: Long,
    val isPersonalBest: Boolean,
    val deltaMs: Long?
)
