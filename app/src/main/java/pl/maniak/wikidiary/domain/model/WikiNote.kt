package pl.maniak.wikidiary.domain.model

import pl.maniak.wikidiary.utils.helpers.formatDateString
import java.util.Date

data class WikiNote(
    val id: Long = 0,
    val tag: String,
    val content: String,
    val category: String? = null,
    val date: Date = Date(),
    val isSend: Boolean = false
) {
    fun formatDateString(): String {
        return formatDateString(date)
    }
}