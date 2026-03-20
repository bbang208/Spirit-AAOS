package io.github.bbang208.spirit.domain.timing

import io.github.bbang208.spirit.domain.tracking.GpsTracker
import io.github.bbang208.spirit.util.Constants
import io.github.bbang208.spirit.util.GeoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LapDetector @Inject constructor(
    private val gpsTracker: GpsTracker
) {
    private var gate: GateEndpoints? = null
    private var startLineBearing: Float = 0f
    private var lastCrossingTimeMs: Long = 0L
    private var lapIndex: Int = 0

    fun configure(startLineLat: Double, startLineLng: Double, startLineBearing: Float) {
        this.startLineBearing = startLineBearing
        gate = GateUtils.computeGateEndpoints(startLineLat, startLineLng, startLineBearing)
        reset()
    }

    fun observeCrossings(): Flow<LapCrossing> = flow {
        val g = gate ?: return@flow

        var prevLat: Double? = null
        var prevLng: Double? = null
        var prevTime: Long? = null
        var prevBearing: Float? = null

        gpsTracker.currentLocation.collect { point ->
            if (point == null) return@collect

            val curLat = point.latitude
            val curLng = point.longitude
            val curTime = point.timestamp
            val curBearing = point.bearing

            if (prevLat != null && prevLng != null && prevTime != null) {
                val intersects = GeoUtils.segmentsIntersect(
                    prevLat!!, prevLng!!,
                    curLat, curLng,
                    g.lat1, g.lng1,
                    g.lat2, g.lng2
                )

                if (intersects) {
                    // Bearing check: travelling in roughly same direction as start line bearing
                    val bearingDiff = GeoUtils.bearingDifference(curBearing, startLineBearing)
                    if (bearingDiff <= Constants.BEARING_TOLERANCE_DEGREES) {
                        // Cooldown check
                        val crossingTime = GeoUtils.interpolateCrossingTime(
                            prevTime!!, prevLat!!, prevLng!!,
                            curTime, curLat, curLng,
                            (g.lat1 + g.lat2) / 2.0, (g.lng1 + g.lng2) / 2.0
                        )

                        if (crossingTime - lastCrossingTimeMs >= Constants.LAP_COOLDOWN_MS) {
                            lastCrossingTimeMs = crossingTime
                            emit(LapCrossing(crossingTime, lapIndex))
                            lapIndex++
                        }
                    }
                }
            }

            prevLat = curLat
            prevLng = curLng
            prevTime = curTime
            prevBearing = curBearing
        }
    }

    fun reset() {
        lastCrossingTimeMs = 0L
        lapIndex = 0
    }
}
