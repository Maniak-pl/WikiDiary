package pl.maniak.wikidiary

import android.database.sqlite.SQLiteException
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.books.data.BookRepositoryImpl
import pl.maniak.wikidiary.feature.books.domain.BookCoverExportContributor
import pl.maniak.wikidiary.feature.books.domain.BookMetadata
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapperImpl
import pl.maniak.wikidiary.feature.diary.domain.NotesExportContributor
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.export.domain.WikiExporter
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class BookRepositoryTest {
    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val books = BookRepositoryImpl(database, WikiNoteMapperImpl())
    private val metadata = BookMetadata(
        "https://lubimyczytac.pl/ksiazka/123/test", "Tytuł", "Autor", 2024,
        "https://s.lubimyczytac.pl/upload/books/0/123/test.jpg"
    )
    private val note = WikiNote(tag = "Book", content = WikiParser.addBookNote("", metadata), date = Date(0))

    @After fun tearDown() = database.close()

    @Test
    fun savesNoteAndMetadataAndExportsLinkAndOneCoverPerBookPerDay() = runBlocking {
        books.saveBookNote(note, metadata)
        books.saveBookNote(note, metadata)
        val entries = books.observeBookNotes().first()
        assertEquals(2, entries.size)
        assertTrue(entries.all { it.metadata == metadata })
        val export = WikiExporter(listOf(
            NotesExportContributor(repositories.notes.observeNotes()),
            BookCoverExportContributor(repositories.notes.observeNotes(), books.observeBookNotes())
        )).document().first()
        val sentence = "Przeczytałem książkę [[${metadata.canonicalUrl}|Tytuł (2024)]] – Autor"
        assertEquals(2, export.split(sentence).size - 1)
        assertEquals(1, export.split("{{ :books:Tytul_123.jpg?200 |}}").size - 1)
    }

    @Test
    fun deleteNoteCascadesToBookMetadata() = runBlocking {
        books.saveBookNote(note, metadata)
        val entry = books.observeBookNotes().first().single()
        val remaining = books.observeBookNotes().nextValueAfter { repositories.notes.deleteNoteById(entry.noteId) }
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun failedMetadataInsertRollsBackNote() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER reject_book BEFORE INSERT ON book_note_table BEGIN SELECT RAISE(ABORT, 'test'); END"
        )
        try {
            books.saveBookNote(note, metadata)
            fail("Expected insert to fail")
        } catch (_: SQLiteException) {
            assertTrue(repositories.notes.getNotes().isEmpty())
            assertTrue(books.observeBookNotes().first().isEmpty())
        }
    }
}
