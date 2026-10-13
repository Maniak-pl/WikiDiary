package pl.maniak.wikidiary.domain.model

import pl.maniak.wikidiary.data.Person
import pl.maniak.wikidiary.utils.helpers.formatDateString
import java.util.Date

data class WikiNote(
    val id: Long = 0,
    val tag: String,
    val content: String,
    val category: String? = null,
    val date: Date,
    val isSend: Boolean = false,
    val people: List<Person> = emptyList()
) {
    fun formatDateString(): String {
        return formatDateString(date)
    }
}