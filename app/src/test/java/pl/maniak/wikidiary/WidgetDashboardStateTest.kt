package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.ui.widget.WidgetDashboardState

class WidgetDashboardStateTest {
    @Test
    fun `note gauge count is clamped to zero through 500`() {
        val negative = state(noteCount = -2)
        val overLimit = state(noteCount = 620)

        assertEquals(0, negative.displayedNoteCount)
        assertEquals(500, overLimit.displayedNoteCount)
    }

    @Test
    fun `fuel gauge counts down from 24 hours since last note`() {
        assertEquals(24, state(hoursSinceLastNote = 0).hoursUntilFuelEmpty)
        assertEquals(10, state(hoursSinceLastNote = 14).hoursUntilFuelEmpty)
        assertEquals(0, state(hoursSinceLastNote = 30).hoursUntilFuelEmpty)
    }

    @Test
    fun `expense warning starts after 24 hours or when no expense exists`() {
        assertFalse(state(hoursSinceLastExpense = 23).needsExpense)
        assertTrue(state(hoursSinceLastExpense = 24).needsExpense)
        assertTrue(state(hoursSinceLastExpense = null).needsExpense)
    }

    @Test
    fun `routine and gratitude warnings reflect today's entries`() {
        val state = state(routineCompletedToday = true, gratitudeAddedToday = true)

        assertFalse(state.needsRoutine)
        assertFalse(state.needsGratitude)
    }

    @Test
    fun `inactivity warning starts at 12 hours`() {
        assertFalse(state(hoursSinceLastNote = 11).needsInactivityWarning)
        assertTrue(state(hoursSinceLastNote = 12).needsInactivityWarning)
    }

    @Test
    fun `timestamp conversion rounds elapsed time down and clamps future timestamps`() {
        val now = 100 * HOUR
        val state = WidgetDashboardState.fromTimestamps(
            noteCount = 0,
            lastNoteTime = now - 14 * HOUR - 30 * 60 * 1000,
            lastExpenseTime = now + HOUR,
            routineCompletedToday = false,
            gratitudeAddedToday = false,
            now = now,
        )

        assertEquals(14, state.hoursSinceLastNote)
        assertEquals(0, state.hoursSinceLastExpense)
    }

    private fun state(
        noteCount: Int = 0,
        hoursSinceLastNote: Int = 0,
        hoursSinceLastExpense: Int? = 23,
        routineCompletedToday: Boolean = false,
        gratitudeAddedToday: Boolean = false,
    ) = WidgetDashboardState(
        noteCount = noteCount,
        hoursSinceLastNote = hoursSinceLastNote,
        hoursSinceLastExpense = hoursSinceLastExpense,
        routineCompletedToday = routineCompletedToday,
        gratitudeAddedToday = gratitudeAddedToday,
    )

    private companion object {
        const val HOUR = 60L * 60L * 1000L
    }
}
