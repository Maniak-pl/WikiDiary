package pl.maniak.wikidiary.core.date

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

/** Material 3 date pickers speak in UTC midnights; the diary speaks in local days. */
fun Date.toPickerMillis(): Long {
    val local = Calendar.getInstance().apply { time = this@toPickerMillis }
    return GregorianCalendar(UTC).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

fun fromPickerMillis(millis: Long): Date {
    val utc = GregorianCalendar(UTC).apply { timeInMillis = millis }
    return GregorianCalendar(
        utc.get(Calendar.YEAR),
        utc.get(Calendar.MONTH),
        utc.get(Calendar.DAY_OF_MONTH)
    ).time
}

private val UTC = TimeZone.getTimeZone("UTC")
