package io.github.bbang208.spirit.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import io.github.bbang208.spirit.domain.timing.SectorState
import io.github.bbang208.spirit.domain.timing.SectorStatus

class SectorBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val density = resources.displayMetrics.density
    private val sectorGap = 2f * density
    private val cornerRadius = 8f * density

    private var sectorStates: List<SectorState> = emptyList()

    fun setSectorStates(states: List<SectorState>) {
        sectorStates = states
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (sectorStates.isEmpty()) return

        val count = sectorStates.size
        val totalGaps = (count - 1) * sectorGap
        val sectorWidth = (width - paddingLeft - paddingRight - totalGaps) / count
        val barHeight = (height - paddingTop - paddingBottom).toFloat()

        var x = paddingLeft.toFloat()

        for (state in sectorStates) {
            paint.color = when (state.status) {
                SectorStatus.PERSONAL_BEST -> COLOR_PERSONAL_BEST
                SectorStatus.COMPLETED -> COLOR_COMPLETED
                SectorStatus.CURRENT -> COLOR_CURRENT
                SectorStatus.PENDING -> COLOR_PENDING
            }

            val rect = RectF(x, paddingTop.toFloat(), x + sectorWidth, paddingTop + barHeight)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            x += sectorWidth + sectorGap
        }
    }

    companion object {
        private const val COLOR_COMPLETED = 0xFFA4A8AE.toInt()     // basic_400
        private const val COLOR_PERSONAL_BEST = 0xFF02C265.toInt() // switch_on
        private const val COLOR_CURRENT = 0xFFE0E1E6.toInt()       // basic_200
        private const val COLOR_PENDING = 0xFF313236.toInt()        // basic_700
    }
}
