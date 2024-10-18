package pl.maniak.wikidiary.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.domain.repository.Config
import pl.maniak.wikidiary.ui.MainActivity
import pl.maniak.wikidiary.ui.model.BatteryInfo

class WikiWidgetProvider(val config: Config) : AppWidgetProvider() {

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
        val batteryLevel = calculateBatteryLevel(lastUpdated)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.noteTextView, context.getString(R.string.add_note))

        val batteryInfo = when (batteryLevel) {
            in 0..20 -> BatteryInfo(R.drawable.ic_battery_20, R.color.red)
            in 21..40 -> BatteryInfo(R.drawable.ic_battery_40, R.color.orange)
            in 41..60 -> BatteryInfo(R.drawable.ic_battery_60, R.color.yellow)
            in 61..80 -> BatteryInfo(R.drawable.ic_battery_80, R.color.green)
            else -> BatteryInfo(R.drawable.ic_battery_100, R.color.white)
        }

        views.setImageViewResource(R.id.batteryImageView, batteryInfo.batteryIcon)
        views.setTextColor(
            R.id.noteTextView,
            ContextCompat.getColor(context, batteryInfo.textColor)
        )

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun calculateBatteryLevel(lastUpdated: Long): Int {
        val currentTime = System.currentTimeMillis()
        val elapsedHours = (currentTime - lastUpdated) / (1000 * 60 * 60)
        return when {
            elapsedHours >= 12 -> 20
            elapsedHours >= 8 -> 40
            elapsedHours >= 6 -> 60
            elapsedHours >= 4 -> 80
            else -> 100
        }
    }
}
