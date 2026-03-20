package io.github.bbang208.spirit.domain.tracking

import io.github.bbang208.spirit.data.models.GpsPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 인제 스피디움 풀코스(3.908km)를 시뮬레이션하는 Mock GPS Provider.
 * OSM 데이터 기반 실제 서킷 좌표 사용.
 */
class MockLocationProvider : LocationProvider {

    private val tracking = AtomicBoolean(false)

    private val trackPoints: List<GpsPoint> by lazy { buildInterpolatedTrack() }

    override fun startTracking(): Flow<GpsPoint> = flow {
        tracking.set(true)
        var index = 0
        while (tracking.get()) {
            val point = trackPoints[index % trackPoints.size]
            emit(
                point.copy(
                    timestamp = System.currentTimeMillis(),
                    accuracy = 3f + (Math.random() * 2).toFloat()
                )
            )
            index++
            delay(100) // 10Hz
        }
    }

    override fun stopTracking() {
        tracking.set(false)
    }

    override fun isTracking(): Boolean = tracking.get()

    // ── Track building ──────────────────────────────────────────────

    private fun buildInterpolatedTrack(): List<GpsPoint> {
        val dense = mutableListOf<LatLng>()

        // Interpolate between waypoints at ~3m intervals
        for (i in 0 until WAYPOINTS.size - 1) {
            val (lat1, lng1) = WAYPOINTS[i]
            val (lat2, lng2) = WAYPOINTS[i + 1]
            val dist = haversine(lat1, lng1, lat2, lng2)
            val steps = (dist / INTERPOLATION_STEP_METERS).toInt().coerceAtLeast(1)

            for (j in 0 until steps) {
                val t = j.toDouble() / steps
                dense.add(LatLng(lat1 + (lat2 - lat1) * t, lng1 + (lng2 - lng1) * t))
            }
        }

        if (dense.isEmpty()) return emptyList()

        // Calculate bearing and speed for each point
        val result = mutableListOf<GpsPoint>()
        for (i in dense.indices) {
            val curr = dense[i]
            val next = dense[(i + 1) % dense.size]
            val bearing = calculateBearing(curr.lat, curr.lng, next.lat, next.lng)

            // Speed based on curvature: look ahead ~15 points for bearing change
            val lookAhead = min(15, dense.size / 2)
            val far = dense[(i + lookAhead) % dense.size]
            val farBearing = calculateBearing(curr.lat, curr.lng, far.lat, far.lng)
            var bearingDiff = abs(bearing - farBearing)
            if (bearingDiff > 180) bearingDiff = 360 - bearingDiff

            // Map curvature to speed: straight → ~150km/h, tight corner → ~60km/h
            val speedKmh = when {
                bearingDiff < 5 -> 140.0 + Math.random() * 20   // straight
                bearingDiff < 15 -> 110.0 + Math.random() * 15  // gentle curve
                bearingDiff < 40 -> 80.0 + Math.random() * 15   // medium curve
                else -> 55.0 + Math.random() * 15               // tight corner
            }
            val speedMs = (speedKmh / 3.6).toFloat()

            result.add(
                GpsPoint(
                    latitude = curr.lat,
                    longitude = curr.lng,
                    altitude = 340.0 + (Math.random() - 0.5) * 2, // ~340m elevation
                    timestamp = 0L, // filled at emit time
                    speed = speedMs,
                    bearing = bearing.toFloat(),
                    accuracy = 3f
                )
            )
        }

        return result
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private data class LatLng(val lat: Double, val lng: Double)

    private fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        return EARTH_RADIUS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun calculateBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLng = Math.toRadians(lng2 - lng1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val y = sin(dLng) * cos(rLat2)
        val x = cos(rLat1) * sin(rLat2) - sin(rLat1) * cos(rLat2) * cos(dLng)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }

    companion object {
        private const val EARTH_RADIUS = 6_371_000.0
        private const val INTERPOLATION_STEP_METERS = 3.0

        /**
         * 인제 스피디움 풀코스 웨이포인트 (OSM Way 651693293 기반, 중복 노드 제거)
         * Start/Finish → T1 헤어핀 → 에세스 → 시케인 → 남쪽 직선 → 남쪽 헤어핀
         * → 서쪽 구간 → 서쪽 헤어핀 → 백스트레이트 → Start/Finish
         */
        private val WAYPOINTS: List<Pair<Double, Double>> = listOf(
            // Start/Finish (북쪽 메인 스트레이트 끝)
            38.0056691 to 128.2936098,

            // T1 진입 ~ 헤어핀 (동쪽 끝)
            38.0041884 to 128.2946504,
            38.0040588 to 128.2946938,
            38.0039374 to 128.2947030,
            38.0038454 to 128.2946407,
            38.0037913 to 128.2945255,
            38.0037737 to 128.2943632,
            38.0038300 to 128.2942221,
            38.0039772 to 128.2940868,

            // 에세스 ~ 시케인 (북쪽 복합 구간)
            38.0049542 to 128.2932955,
            38.0050167 to 128.2927056,
            38.0050468 to 128.2928778,
            38.0049558 to 128.2925612,
            38.0044720 to 128.2922429,
            38.0042151 to 128.2922953,
            38.0039628 to 128.2924814,
            38.0038477 to 128.2926989,

            // 크로스오버 → 동쪽 직선 (남쪽으로)
            38.0032459 to 128.2939751,
            38.0031614 to 128.2940912,
            38.0030567 to 128.2941751,
            38.0017052 to 128.2941199,
            38.0006990 to 128.2940594,
            38.0005863 to 128.2940370,
            38.0004351 to 128.2938599,
            38.0003569 to 128.2935530,
            38.0002600 to 128.2933167,
            38.0001739 to 128.2931302,
            38.0000881 to 128.2929917,
            37.9999732 to 128.2928399,
            37.9998371 to 128.2926961,
            37.9994602 to 128.2924176,

            // 남동쪽 구간
            37.9985471 to 128.2919457,
            37.9984391 to 128.2919613,
            37.9977867 to 128.2924094,
            37.9976614 to 128.2923645,
            37.9975209 to 128.2921516,
            37.9975065 to 128.2919621,
            37.9975336 to 128.2917949,
            37.9975715 to 128.2915610,
            37.9975809 to 128.2914083,
            37.9970219 to 128.2910221,
            37.9968228 to 128.2905854,

            // 남쪽 끝 → 서쪽으로 전환
            37.9967579 to 128.2894025,
            37.9982048 to 128.2884197,
            37.9989894 to 128.2871866,
            37.9990941 to 128.2869028,
            37.9991260 to 128.2866940,

            // 서쪽 직선
            37.9991378 to 128.2856637,

            // 서쪽 헤어핀
            37.9992418 to 128.2853888,
            37.9993427 to 128.2853510,
            37.9994569 to 128.2853610,
            37.9995961 to 128.2854055,
            37.9996698 to 128.2854936,
            37.9997207 to 128.2855817,

            // 동쪽으로 복귀
            37.9999420 to 128.2867116,
            37.9998628 to 128.2869479,
            37.9997836 to 128.2871092,

            // 남쪽 리턴 루프
            37.9979400 to 128.2895835,
            37.9978937 to 128.2896917,
            37.9978910 to 128.2898615,
            37.9979657 to 128.2901950,
            37.9980834 to 128.2907178,
            37.9981848 to 128.2909229,

            // 백스트레이트 진입
            37.9987469 to 128.2913240,
            37.9989325 to 128.2913463,

            // 메인 스트레이트 (loop close)
            38.0056691 to 128.2936098,
        )
    }
}
