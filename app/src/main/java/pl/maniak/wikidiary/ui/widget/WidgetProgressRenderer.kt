package pl.maniak.wikidiary.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import pl.maniak.wikidiary.R
import kotlin.math.min

internal object WidgetProgressRenderer {
    fun render(context: Context, hoursSinceLastNote: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = (400 * density).toInt().coerceAtLeast(1)
        val height = (8 * density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val elapsedHours = hoursSinceLastNote.coerceIn(0, WidgetDashboardState.FUEL_HOURS)
        val filledSegments = WidgetDashboardState.FUEL_HOURS - elapsedHours
        val progress = elapsedHours / WidgetDashboardState.FUEL_HOURS.toFloat()
        val color = when {
            progress <= .25f -> R.color.green
            progress <= .5f -> R.color.yellow
            progress <= .75f -> R.color.orange
            else -> R.color.red
        }
        val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val segmentGap = 2f * density
        val segmentWidth = (width - segmentGap * (WidgetDashboardState.FUEL_HOURS - 1)) /
            WidgetDashboardState.FUEL_HOURS
        val radius = min(height / 2f, segmentWidth / 2f)

        repeat(WidgetDashboardState.FUEL_HOURS) { index ->
            segmentPaint.color = ContextCompat.getColor(
                context,
                if (index < filledSegments) color else R.color.widget_indicator_inactive,
            )
            val left = index * (segmentWidth + segmentGap)
            canvas.drawRoundRect(
                left,
                0f,
                left + segmentWidth,
                height.toFloat(),
                radius,
                radius,
                segmentPaint,
            )
        }
        return bitmap
    }
}
