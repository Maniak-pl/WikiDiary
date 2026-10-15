package pl.maniak.wikidiary.feature.books.domain

import java.text.Normalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportItem
import pl.maniak.wikidiary.feature.export.domain.toExportDayKey

class BookCoverExportContributor(
    private val notes: Flow<List<WikiNote>>,
    private val books: Flow<List<BookNoteEntry>>,
) : WikiExportContributor {
    override val order: Int = 30

    override fun items(): Flow<List<WikiExportItem>> = combine(notes, books) { notes, books ->
        val byNote = books.associateBy(BookNoteEntry::noteId)
        val seen = mutableSetOf<Pair<String, String>>()
        notes.sortedWith(compareBy(WikiNote::date, WikiNote::id)).mapNotNull { note ->
            val book = byNote[note.id]?.metadata ?: return@mapNotNull null
            if (book.coverUrl == null || !seen.add(note.date.toExportDayKey() to book.canonicalUrl)) {
                return@mapNotNull null
            }
            WikiExportItem.DayAttachment(note.date, "{{ :books:${coverFileName(book)}?200 |}}")
        }
    }

    companion object {
        fun coverFileName(book: BookMetadata): String {
            val title = Normalizer.normalize(book.title.replace('ł', 'l').replace('Ł', 'L'), Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
                .replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').take(100).ifBlank { "Book" }
            val id = book.canonicalUrl.substringAfter("/ksiazka/").substringBefore('/')
            return "${title}_$id.jpg"
        }
    }
}
