package io.github.bbang208.spirit.util

object Constants {
    const val GPS_SAMPLE_RATE_HZ = 10
    const val GPS_INTERVAL_MS = 100L
    const val GPS_MIN_ACCURACY_METERS = 15f
    const val LAP_COOLDOWN_MS = 15_000L
    const val GATE_WIDTH_METERS = 30.0
    const val BEARING_TOLERANCE_DEGREES = 90f
    const val TRACK_CLOSE_DISTANCE_METERS = 50.0
    const val GHOST_SAMPLE_RATE_HZ = 2
    const val GPS_BUFFER_SIZE = 1000
    const val GPS_BATCH_INTERVAL_MS = 2000L
    const val SPEED_MS_TO_KMH = 3.6f
    const val G_FORCE_FILTER_ALPHA = 0.3f
}
