package io.github.bbang208.spirit.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import io.github.bbang208.spirit.data.models.GpsPoint
import io.github.bbang208.spirit.data.models.Sector

class TrackMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TRACK
        strokeWidth = TRACK_STROKE_DP * resources.displayMetrics.density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val startMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TRACK
        style = Paint.Style.FILL
    }

    private val sectorMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_SECTOR
        style = Paint.Style.FILL
    }

    private val sectorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 10f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
    }

    private var points: List<GpsPoint> = emptyList()
    private var sectors: List<Sector> = emptyList()
    private var isLiveMode = false

    private var minLat = 0.0
    private var maxLat = 0.0
    private var minLng = 0.0
    private var maxLng = 0.0
    private var scale = 1.0
    private var offsetX = 0f
    private var offsetY = 0f

    fun setTrackData(points: List<GpsPoint>, sectors: List<Sector> = emptyList()) {
        this.points = points
        this.sectors = sectors
        this.isLiveMode = false
        computeProjection()
        invalidate()
    }

    fun setLivePoints(points: List<GpsPoint>) {
        this.points = points
        this.sectors = emptyList()
        this.isLiveMode = true
        computeProjection()
        invalidate()
    }

    private fun computeProjection() {
        if (points.size < 2) return

        minLat = points.minOf { it.latitude }
        maxLat = points.maxOf { it.latitude }
        minLng = points.minOf { it.longitude }
        maxLng = points.maxOf { it.longitude }

        // Add 10% padding
        val latRange = (maxLat - minLat).coerceAtLeast(0.00001)
        val lngRange = (maxLng - minLng).coerceAtLeast(0.00001)
        val padding = 0.1
        minLat -= latRange * padding
        maxLat += latRange * padding
        minLng -= lngRange * padding
        maxLng += lngRange * padding

        val viewWidth = (width - paddingLeft - paddingRight).toFloat()
        val viewHeight = (height - paddingTop - paddingBottom).toFloat()
        if (viewWidth <= 0 || viewHeight <= 0) return

        val latSpan = maxLat - minLat
        val lngSpan = maxLng - minLng

        val scaleX = viewWidth / lngSpan
        val scaleY = viewHeight / latSpan
        scale = minOf(scaleX, scaleY)

        // Center the track
        val projectedWidth = lngSpan * scale
        val projectedHeight = latSpan * scale
        offsetX = paddingLeft + (viewWidth - projectedWidth.toFloat()) / 2f
        offsetY = paddingTop + (viewHeight - projectedHeight.toFloat()) / 2f
    }

    private fun toScreenX(lng: Double): Float {
        return (offsetX + (lng - minLng) * scale).toFloat()
    }

    private fun toScreenY(lat: Double): Float {
        // Invert Y: higher lat = higher on screen
        return (offsetY + (maxLat - lat) * scale).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeProjection()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size < 2) return

        // Draw track path
        val path = Path()
        path.moveTo(toScreenX(points[0].longitude), toScreenY(points[0].latitude))
        for (i in 1 until points.size) {
            path.lineTo(toScreenX(points[i].longitude), toScreenY(points[i].latitude))
        }
        canvas.drawPath(path, trackPaint)

        val markerRadius = START_MARKER_DP * resources.displayMetrics.density

        // Draw start point marker
        val startX = toScreenX(points[0].longitude)
        val startY = toScreenY(points[0].latitude)
        canvas.drawCircle(startX, startY, markerRadius, startMarkerPaint)

        // Draw sector markers
        val sectorRadius = SECTOR_MARKER_DP * resources.displayMetrics.density
        for (sector in sectors) {
            val sx = toScreenX(sector.gateLng)
            val sy = toScreenY(sector.gateLat)
            canvas.drawCircle(sx, sy, sectorRadius, sectorMarkerPaint)

            val label = "S${sector.index}"
            val textY = sy + sectorTextPaint.textSize / 3f
            canvas.drawText(label, sx, textY, sectorTextPaint)
        }
    }

    companion object {
        private const val COLOR_TRACK = 0xFF02C265.toInt()
        private const val COLOR_SECTOR = 0xFF9E9E9E.toInt()
        private const val TRACK_STROKE_DP = 4f
        private const val START_MARKER_DP = 8f
        private const val SECTOR_MARKER_DP = 6f
    }
}
