package pl.maniak.wikidiary.ui.widget

data class WidgetDashboardState(
    val noteCount: Int,
    val hoursSinceLastNote: Int,
    val hoursSinceLastExpense: Int?,
    val routineCompletedToday: Boolean,
    val gratitudeAddedToday: Boolean,
) {
    val displayedNoteCount: Int
        get() = noteCount.coerceIn(0, NOTE_GAUGE_MAX)

    val hoursUntilFuelEmpty: Int
        get() = (FUEL_HOURS - hoursSinceLastNote).coerceIn(0, FUEL_HOURS)

    val needsExpense: Boolean
        get() = hoursSinceLastExpense == null || hoursSinceLastExpense >= EXPENSE_WARNING_HOURS

    val needsRoutine: Boolean
        get() = !routineCompletedToday

    val needsInactivityWarning: Boolean
        get() = hoursSinceLastNote >= INACTIVITY_WARNING_HOURS

    val needsGratitude: Boolean
        get() = !gratitudeAddedToday

    companion object {
        const val NOTE_GAUGE_MAX = 500
        const val FUEL_HOURS = 24
        const val EXPENSE_WARNING_HOURS = 24
        const val INACTIVITY_WARNING_HOURS = 12

        fun fromTimestamps(
            noteCount: Int,
            lastNoteTime: Long,
            lastExpenseTime: Long?,
            routineCompletedToday: Boolean,
            gratitudeAddedToday: Boolean,
            now: Long,
        ): WidgetDashboardState = WidgetDashboardState(
            noteCount = noteCount,
            hoursSinceLastNote = elapsedHours(lastNoteTime, now),
            hoursSinceLastExpense = lastExpenseTime?.let { elapsedHours(it, now) },
            routineCompletedToday = routineCompletedToday,
            gratitudeAddedToday = gratitudeAddedToday,
        )

        private fun elapsedHours(timestamp: Long, now: Long): Int {
            if (timestamp <= 0L) return MAX_DISPLAYED_HOURS
            return ((now - timestamp).coerceAtLeast(0L) / MILLIS_PER_HOUR)
                .coerceAtMost(MAX_DISPLAYED_HOURS.toLong())
                .toInt()
        }

        private const val MILLIS_PER_HOUR = 60L * 60L * 1000L
        private const val MAX_DISPLAYED_HOURS = 999
    }
}
