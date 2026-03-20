package io.github.bbang208.spirit.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GForceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_GRID
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }

    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_GRID
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_DOT
        style = Paint.Style.FILL
    }

    private val dotRadius = 6f * density

    private var lateralG = 0f
    private var longitudinalG = 0f

    fun setGForce(lateralG: Float, longitudinalG: Float) {
        this.lateralG = lateralG
        this.longitudinalG = longitudinalG
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) - 4f * density

        // Draw concentric grid circles (0.5G intervals, 4 circles for 2G max)
        for (i in 1..GRID_CIRCLES) {
            val r = radius * i / GRID_CIRCLES
            canvas.drawCircle(cx, cy, r, gridPaint)
        }

        // Draw crosshair
        canvas.drawLine(cx - radius, cy, cx + radius, cy, crosshairPaint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, crosshairPaint)

        // Draw G-Force dot
        // Clamp to max range
        val clampedLateral = lateralG.coerceIn(-MAX_G, MAX_G)
        val clampedLongitudinal = longitudinalG.coerceIn(-MAX_G, MAX_G)

        val dotX = cx + (clampedLateral / MAX_G) * radius
        val dotY = cy - (clampedLongitudinal / MAX_G) * radius // Y inverted

        canvas.drawCircle(dotX, dotY, dotRadius, dotPaint)
    }

    companion object {
        private const val COLOR_GRID = 0xFF44464E.toInt()      // basic_600
        private const val COLOR_DOT = 0xFF02C265.toInt()       // switch_on
        private const val MAX_G = 2f
        private const val GRID_CIRCLES = 4
    }
}
