package pl.maniak.wikidiary.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import org.koin.java.KoinJavaComponent.inject
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.domain.repository.Config
import pl.maniak.wikidiary.ui.MainActivity

class WikiWidgetProvider : AppWidgetProvider() {

    private val config: Config by inject(Config::class.java)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val lastUpdated = config.getLastUpdated()
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val hoursSinceLastUpdate = calculateElapsedHours(lastUpdated)
        val gridContent = generateSquares(hoursSinceLastUpdate)
        val (color, textColor) = getColorForProgress(hoursSinceLastUpdate)

        views.setTextViewText(R.id.squaresTextView, gridContent)
        views.setTextViewText(
            R.id.noteTextView,
            context.getString(R.string.last_note_time, hoursSinceLastUpdate)
        )
        views.setTextColor(R.id.noteTextView, ContextCompat.getColor(context, textColor))
        views.setTextColor(R.id.squaresTextView, ContextCompat.getColor(context, color))

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun calculateElapsedHours(lastUpdated: Long): Int {
        val currentTime = System.currentTimeMillis()
        val elapsedMillis = currentTime - lastUpdated
        return (elapsedMillis / (1000 * 60 * 60)).toInt()
    }

    private fun generateSquares(hoursSinceLastUpdate: Int): String {
        val filled = 24 - hoursSinceLastUpdate
        val empty = hoursSinceLastUpdate
        return "■".repeat(filled) + "□".repeat(empty)
    }

    private fun getColorForProgress(hoursSinceLastUpdate: Int): Pair<Int, Int> {
        val progressPercentage: Double = (hoursSinceLastUpdate / 24.0) * 100

        return when {
            progressPercentage <= 25 -> Pair(R.color.green, R.color.white)
            progressPercentage <= 50 -> Pair(R.color.yellow, R.color.white)
            progressPercentage <= 75 -> Pair(R.color.orange, R.color.white)
            else -> Pair(R.color.red, R.color.red)
        }
    }
}
