package io.github.bbang208.spirit.util

import io.github.bbang208.spirit.data.models.GpsPoint
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * asin(sqrt(a))
        return EARTH_RADIUS_METERS * c
    }

    fun segmentsIntersect(
        p1Lat: Double, p1Lng: Double,
        p2Lat: Double, p2Lng: Double,
        p3Lat: Double, p3Lng: Double,
        p4Lat: Double, p4Lng: Double
    ): Boolean {
        val d1 = crossProduct(p3Lat, p3Lng, p4Lat, p4Lng, p1Lat, p1Lng)
        val d2 = crossProduct(p3Lat, p3Lng, p4Lat, p4Lng, p2Lat, p2Lng)
        val d3 = crossProduct(p1Lat, p1Lng, p2Lat, p2Lng, p3Lat, p3Lng)
        val d4 = crossProduct(p1Lat, p1Lng, p2Lat, p2Lng, p4Lat, p4Lng)

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
        ) return true

        return false
    }

    private fun crossProduct(
        ax: Double, ay: Double,
        bx: Double, by: Double,
        cx: Double, cy: Double
    ): Double {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
    }

    fun interpolateCrossingTime(
        t1: Long, lat1: Double, lng1: Double,
        t2: Long, lat2: Double, lng2: Double,
        gateLat: Double, gateLng: Double
    ): Long {
        val d1 = haversineDistance(lat1, lng1, gateLat, gateLng)
        val d2 = haversineDistance(lat2, lng2, gateLat, gateLng)
        val ratio = if (d1 + d2 > 0) d1 / (d1 + d2) else 0.5
        return t1 + ((t2 - t1) * ratio).toLong()
    }

    fun bearingDifference(b1: Float, b2: Float): Float {
        var diff = b2 - b1
        while (diff > 180) diff -= 360
        while (diff < -180) diff += 360
        return abs(diff)
    }

    fun douglasPeucker(points: List<GpsPoint>, epsilon: Double): List<GpsPoint> {
        if (points.size < 3) return points

        var maxDist = 0.0
        var maxIdx = 0
        val first = points.first()
        val last = points.last()

        for (i in 1 until points.size - 1) {
            val dist = perpendicularDistance(
                points[i].latitude, points[i].longitude,
                first.latitude, first.longitude,
                last.latitude, last.longitude
            )
            if (dist > maxDist) {
                maxDist = dist
                maxIdx = i
            }
        }

        return if (maxDist > epsilon) {
            val left = douglasPeucker(points.subList(0, maxIdx + 1), epsilon)
            val right = douglasPeucker(points.subList(maxIdx, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(first, last)
        }
    }

    private fun perpendicularDistance(
        pLat: Double, pLng: Double,
        aLat: Double, aLng: Double,
        bLat: Double, bLng: Double
    ): Double {
        val ab = haversineDistance(aLat, aLng, bLat, bLng)
        if (ab == 0.0) return haversineDistance(pLat, pLng, aLat, aLng)

        val ap = haversineDistance(aLat, aLng, pLat, pLng)
        val bp = haversineDistance(bLat, bLng, pLat, pLng)

        val s = (ab + ap + bp) / 2
        val area = sqrt(s * (s - ab) * (s - ap) * (s - bp).coerceAtLeast(0.0))
        return 2 * area / ab
    }
}
