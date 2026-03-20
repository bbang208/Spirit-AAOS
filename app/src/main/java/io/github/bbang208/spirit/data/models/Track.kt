package io.github.bbang208.spirit.data.models

data class Track(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val grade: String,
    val startLineLat: Double,
    val startLineLng: Double,
    val startLineBearing: Float,
    val lengthMeters: Float,
    val outlinePoints: List<GpsPoint>,
    val sectors: List<Sector>,
    val isLocal: Boolean,
    val isPreset: Boolean,
    val remoteId: String?
)
