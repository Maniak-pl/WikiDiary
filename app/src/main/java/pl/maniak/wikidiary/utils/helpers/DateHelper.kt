package pl.maniak.wikidiary.utils.helpers

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateHelper {

    private const val dayNameFormat = "dd.MM.yyyy, EEEE"
    private const val dayFormat = "dd"
    private const val yearFormat = "yyyy"

    fun Date.toFormattedStringWithDayName(): String {
        return SimpleDateFormat(dayNameFormat, Locale.US).format(this)
    }
    fun Date.toDayString(): String {
        return SimpleDateFormat(dayFormat, Locale.getDefault()).format(this)
    }

    fun Date.toYearString(): String {
        return SimpleDateFormat(yearFormat, Locale.getDefault()).format(this)
    }

    fun getOnlyDayFromDate(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.timeZone = TimeZone.getDefault()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return SimpleDateFormat(dayFormat, Locale.getDefault()).format(cal.time)
    }
}

fun formatDateString(date: Date): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(date)
}