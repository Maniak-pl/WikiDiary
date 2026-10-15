package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.books.domain.BookCoverExportContributor
import pl.maniak.wikidiary.feature.books.domain.BookMetadata
import pl.maniak.wikidiary.feature.books.domain.BookNoteEntry
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.export.domain.WikiExportItem

class BookCoverExportTest {
    private val book = BookMetadata("https://lubimyczytac.pl/ksiazka/123/test", "Łódź: świat!", "Autor",
        coverUrl = "https://s.lubimyczytac.pl/upload/books/0/123/test.jpg")

    @Test
    fun differentBooksWithSameTitleHaveDistinctFileNames() {
        assertEquals("Lodz_swiat_123.jpg", BookCoverExportContributor.coverFileName(book))
        assertEquals("Lodz_swiat_456.jpg", BookCoverExportContributor.coverFileName(
            book.copy(canonicalUrl = "https://lubimyczytac.pl/ksiazka/456/test")
        ))
    }

    @Test
    fun repeatsCoverOnDifferentDaysButNotWhenMissingOrNoteIsDeleted() = runBlocking {
        val notes = listOf(
            WikiNote(id = 1, tag = "Book", content = "", date = Date(0)),
            WikiNote(id = 2, tag = "Book", content = "", date = Date(86_400_000)),
            WikiNote(id = 3, tag = "Book", content = "", date = Date(0))
        )
        val entries = listOf(
            BookNoteEntry(1, book), BookNoteEntry(2, book),
            BookNoteEntry(3, book.copy(coverUrl = null)), BookNoteEntry(4, book)
        )
        val result = BookCoverExportContributor(flowOf(notes), flowOf(entries)).items().first()
        assertEquals(2, result.size)
        assertEquals(notes.take(2).map { it.date }, result.map(WikiExportItem::date))
    }

    @Test
    fun optionalYearIsNotRenderedAsNullAndDefaultPrefixIsUsed() {
        val text = WikiParser.addBookNote(" ", book)
        assertEquals("Przeczytałem książkę [[${book.canonicalUrl}|Łódź: świat!]] – Autor", text)
        assertFalse(text.contains("null"))
    }
}
