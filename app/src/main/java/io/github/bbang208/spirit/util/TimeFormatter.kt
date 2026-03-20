package io.github.bbang208.spirit.util

object TimeFormatter {

    fun formatLapTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = timeMs % 1000
        return "%d:%02d.%03d".format(minutes, seconds, millis)
    }

    fun formatDelta(deltaMs: Long): String {
        val prefix = if (deltaMs >= 0) "+" else "-"
        val absMs = kotlin.math.abs(deltaMs)
        val seconds = absMs / 1000
        val millis = absMs % 1000
        return "%s%d.%03d".format(prefix, seconds, millis)
    }
}
