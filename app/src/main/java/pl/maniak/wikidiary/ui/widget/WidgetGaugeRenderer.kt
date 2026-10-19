package pl.maniak.wikidiary.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import pl.maniak.wikidiary.R
import kotlin.math.cos
import kotlin.math.sin

internal object WidgetGaugeRenderer {
    fun render(
        context: Context,
        title: String,
        value: Int,
        max: Int,
        labels: List<Int>,
        subtitle: String,
        isFuel: Boolean,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (200 * density).toInt()
        val scaleFactor = size / 200f
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { scale(scaleFactor, scaleFactor) }
        val cx = 100f
        val cy = 100f
        val radius = 82f
        val accent = ContextCompat.getColor(
            context,
            if (isFuel) R.color.yellow else R.color.orange
        )
        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF090B0E.toInt() }
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF737B82.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            strokeWidth = 1.3f
        }
        val majorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            strokeWidth = 3.5f
            strokeCap = Paint.Cap.ROUND
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE8D9CE.toInt()
            textSize = 9f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = .08f
        }
        val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFF5B55.toInt()
            strokeWidth = 4f
            strokeCap = Paint.Cap.ROUND
        }
        val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFF5B55.toInt() }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFF5EC.toInt()
            textSize = if (isFuel) 22f else 27f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFB9BEC3.toInt()
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawCircle(cx, cy, radius, facePaint)
        canvas.drawCircle(cx, cy, radius, ringPaint)
        canvas.drawCircle(cx, cy, radius - 8, ringPaint.apply { strokeWidth = .8f })

        val step = if (isFuel) 2 else 25
        for (tick in 0..max step step) {
            val angle = Math.toRadians((135.0 + 270.0 * tick / max))
            val major = if (isFuel) tick % 6 == 0 else tick % 50 == 0
            val innerRadius = radius - if (major) 13f else 8f
            val x1 = cx + cos(angle).toFloat() * innerRadius
            val y1 = cy + sin(angle).toFloat() * innerRadius
            val x2 = cx + cos(angle).toFloat() * radius
            val y2 = cy + sin(angle).toFloat() * radius
            canvas.drawLine(x1, y1, x2, y2, if (major) majorTickPaint else tickPaint)
            if (tick in labels) {
                val labelRadius = radius - 21f
                canvas.drawText(
                    tick.toString(),
                    cx + cos(angle).toFloat() * labelRadius,
                    cy + sin(angle).toFloat() * labelRadius + 3f,
                    labelPaint,
                )
            }
        }

        val needleAngle = Math.toRadians(135.0 + 270.0 * value.coerceIn(0, max) / max)
        canvas.drawLine(
            cx,
            cy,
            cx + cos(needleAngle).toFloat() * 52f,
            cy + sin(needleAngle).toFloat() * 52f,
            needlePaint,
        )
        canvas.drawCircle(cx, cy, 6f, hubPaint)

        val titleBox = RectF(cx - 34f, 59f, cx + 34f, 76f)
        canvas.drawRoundRect(titleBox, 8f, 8f, facePaint)
        canvas.drawRoundRect(titleBox, 8f, 8f, ringPaint.apply { strokeWidth = .7f })
        canvas.drawText(title, cx, 70f, titlePaint)
        canvas.drawText(if (isFuel) "${value}h" else value.toString(), cx, 129f, valuePaint)
        if (subtitle.isNotEmpty()) {
            canvas.drawText(subtitle, cx, 151f, subtitlePaint)
        }
        return bitmap
    }
}
