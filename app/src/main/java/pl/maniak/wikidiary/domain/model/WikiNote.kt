package pl.maniak.wikidiary.domain.model

import pl.maniak.wikidiary.utils.helpers.formatDateString
import java.util.Date

data class WikiNote(
    val id: Long,
    val tag: String,
    val content: String,
    val category: String? = null,
    val date: Date,
    val isSend: Boolean
) {
    fun formatDateString(): String {
        return formatDateString(date)
    }
}