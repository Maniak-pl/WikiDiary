package pl.maniak.wikidiary

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import pl.maniak.wikidiary.core.date.fromPickerMillis
import pl.maniak.wikidiary.core.date.toPickerMillis

class PickerDatesTest {

    private val originalZone = TimeZone.getDefault()

    @After
    fun tearDown() = TimeZone.setDefault(originalZone)

    @Test
    fun pickerShowsTheSameDayAsTheDiaryJustAfterLocalMidnight() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))
        val justAfterMidnight = GregorianCalendar(2026, Calendar.OCTOBER, 13, 0, 30).time

        assertEquals(utcMidnight(2026, Calendar.OCTOBER, 13), justAfterMidnight.toPickerMillis())
    }

    @Test
    fun pickedDayBecomesThatLocalDayBehindUtc() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))

        val picked = fromPickerMillis(utcMidnight(2026, Calendar.OCTOBER, 13))

        assertEquals(GregorianCalendar(2026, Calendar.OCTOBER, 13).time, picked)
    }

    private fun utcMidnight(year: Int, month: Int, day: Int): Long =
        GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day)
        }.timeInMillis
}
