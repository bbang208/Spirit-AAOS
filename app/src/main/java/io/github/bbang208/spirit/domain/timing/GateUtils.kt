package io.github.bbang208.spirit.domain.timing

import io.github.bbang208.spirit.util.Constants
import kotlin.math.cos
import kotlin.math.sin

object GateUtils {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun computeGateEndpoints(
        centerLat: Double,
        centerLng: Double,
        bearing: Float,
        widthMeters: Double = Constants.GATE_WIDTH_METERS
    ): GateEndpoints {
        val halfWidth = widthMeters / 2.0

        // Perpendicular bearings (left and right of the gate bearing)
        val perpLeft = bearing - 90f
        val perpRight = bearing + 90f

        val (lat1, lng1) = destinationPoint(centerLat, centerLng, perpLeft.toDouble(), halfWidth)
        val (lat2, lng2) = destinationPoint(centerLat, centerLng, perpRight.toDouble(), halfWidth)

        return GateEndpoints(lat1, lng1, lat2, lng2)
    }

    private fun destinationPoint(
        lat: Double,
        lng: Double,
        bearingDeg: Double,
        distanceMeters: Double
    ): Pair<Double, Double> {
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val bearingRad = Math.toRadians(bearingDeg)
        val angularDistance = distanceMeters / EARTH_RADIUS_METERS

        val destLatRad = Math.asin(
            sin(latRad) * cos(angularDistance) +
                    cos(latRad) * sin(angularDistance) * cos(bearingRad)
        )
        val destLngRad = lngRad + Math.atan2(
            sin(bearingRad) * sin(angularDistance) * cos(latRad),
            cos(angularDistance) - sin(latRad) * sin(destLatRad)
        )

        return Pair(Math.toDegrees(destLatRad), Math.toDegrees(destLngRad))
    }
}
