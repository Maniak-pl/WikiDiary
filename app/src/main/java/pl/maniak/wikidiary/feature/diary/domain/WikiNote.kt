package pl.maniak.wikidiary.feature.diary.domain

import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.core.util.formatDateString
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