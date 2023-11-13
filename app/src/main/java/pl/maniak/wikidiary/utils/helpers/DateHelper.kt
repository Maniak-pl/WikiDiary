package pl.maniak.wikidiary.utils.helpers

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    private const val dayFormat = "dd"

    fun getOnlyDayFromDate(date: Date): String {
        return SimpleDateFormat(dayFormat, Locale.getDefault()).format(date)
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