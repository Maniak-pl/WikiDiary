package pl.maniak.wikidiary.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import org.koin.java.KoinJavaComponent.inject
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.ui.MainActivity
import java.util.Calendar

class WikiWidgetProvider : AppWidgetProvider() {

    private val config: Config by inject(Config::class.java)
    private val database: WikiNoteDatabase by inject(WikiNoteDatabase::class.java)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = loadDashboardState()
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId, state)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to update dashboard widget", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        state: WidgetDashboardState,
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val hoursSinceLastUpdate = state.hoursSinceLastNote
        views.setImageViewBitmap(
            R.id.progressBarImage,
            WidgetProgressRenderer.render(context, hoursSinceLastUpdate),
        )
        views.setImageViewBitmap(
            R.id.speedGaugeImage,
            WidgetGaugeRenderer.render(
                context = context,
                title = "NOTATKI",
                value = state.displayedNoteCount,
                max = WidgetDashboardState.NOTE_GAUGE_MAX,
                labels = listOf(0, 50, 100, 200, 300, 400, 500),
                subtitle = "LICZBA NOTATEK",
                isFuel = false,
            ),
        )
        views.setImageViewBitmap(
            R.id.fuelGaugeImage,
            WidgetGaugeRenderer.render(
                context = context,
                title = "CZAS",
                value = state.hoursUntilFuelEmpty,
                max = WidgetDashboardState.FUEL_HOURS,
                labels = listOf(0, 6, 12, 18, 24),
                subtitle = "",
                isFuel = true,
            ),
        )
        setIndicator(views, R.id.gratitudeIndicatorIcon, "❤️", state.needsGratitude)
        setIndicator(views, R.id.activityIndicatorIcon, "🕒", state.needsInactivityWarning)
        setIndicator(views, R.id.routineIndicatorIcon, "\uD83C\uDFAF", state.needsRoutine)
        setIndicator(views, R.id.expenseIndicatorIcon, "💰", state.needsExpense)

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

    private fun loadDashboardState(): WidgetDashboardState {
        val now = System.currentTimeMillis()
        val dayStart = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStartMillis = dayStart.timeInMillis
        dayStart.add(Calendar.DAY_OF_YEAR, 1)
        val notes = database.wikiNoteDao()
        return WidgetDashboardState.fromTimestamps(
            noteCount = notes.getNoteCount(),
            lastNoteTime = config.getLastUpdated(),
            lastExpenseTime = database.expenseDao().getLatestExpenseDate(),
            routineCompletedToday = notes.hasNoteWithTagBetween(ROUTINE_TAG, dayStartMillis, dayStart.timeInMillis),
            gratitudeAddedToday = notes.hasNoteWithTagBetween(GRATITUDE_TAG, dayStartMillis, dayStart.timeInMillis),
            now = now,
        )
    }

    private fun setIndicator(
        views: RemoteViews,
        iconId: Int,
        icon: String,
        isActive: Boolean,
    ) {
        views.setViewVisibility(iconId, if (isActive) View.VISIBLE else View.INVISIBLE)
        views.setTextViewText(iconId, icon)
    }

    private companion object {
        const val TAG = "WikiWidgetProvider"
        const val ROUTINE_TAG = "Routine"
        const val GRATITUDE_TAG = "Kronika wdzięczności"
    }
}
