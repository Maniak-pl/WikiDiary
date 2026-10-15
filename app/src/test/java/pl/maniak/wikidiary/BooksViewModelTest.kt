package pl.maniak.wikidiary

import android.database.sqlite.SQLiteException
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.books.domain.BookMetadata
import pl.maniak.wikidiary.feature.books.domain.BookNoteEntry
import pl.maniak.wikidiary.feature.books.domain.BookRepository
import pl.maniak.wikidiary.feature.books.domain.BookSearchRepository
import pl.maniak.wikidiary.feature.books.ui.BooksIntent
import pl.maniak.wikidiary.feature.books.ui.BooksViewModel
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.testing.InMemoryConfig
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase

@RunWith(RobolectricTestRunner::class)
class BooksViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()
    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val config = InMemoryConfig()
    private val date = SelectedDateStore(Date(1_728_480_000_000))
    private val book = BookMetadata(
        "https://lubimyczytac.pl/ksiazka/123/test", "Tytuł", "Autor", 2024,
        "https://s.lubimyczytac.pl/upload/books/0/123/test.jpg"
    )
    private val search = FakeSearch()
    private val repository = RecordingBooks()
    private val viewModel by lazy { BooksViewModel(search, repository, repositories.tags, date, config) }

    @After fun tearDown() = database.close()

    private suspend fun openAndSelect() {
        repositories.tags.saveTag(Tag(0, "Book"))
        viewModel.open("Tytuł", "sheet")
        withTimeout(5_000) { viewModel.uiState.first { it.tags.isNotEmpty() } }
        viewModel.onIntent(BooksIntent.Select(book))
    }

    @Test
    fun openingSearchesInitialQueryAndLoadsDetailsOnlyOnSelection() = runBlocking {
        openAndSelect()
        assertEquals(listOf("Tytuł"), search.queries)
        assertEquals(listOf(book.canonicalUrl), search.detailsCalls)
        assertEquals(book, viewModel.uiState.value.selected)
        assertEquals("2024", viewModel.uiState.value.year)
        assertEquals("Book", viewModel.uiState.value.tag?.name)
    }

    @Test
    fun savesEditedBookOnSelectedDateAndBlocksDuplicateSubmission() = runBlocking {
        openAndSelect()
        val selectedDate = Date(date.date.value.time - 86_400_000)
        date.select(selectedDate)
        viewModel.onIntent(BooksIntent.Title("Nowy tytuł"))
        viewModel.onIntent(BooksIntent.Author("Nowy autor"))
        viewModel.onIntent(BooksIntent.Year(""))
        viewModel.onIntent(BooksIntent.Prefix("Przeczytałam"))
        repository.gate = CompletableDeferred()
        viewModel.onIntent(BooksIntent.Save)
        viewModel.onIntent(BooksIntent.Save)
        assertEquals(1, repository.saved.size)
        assertTrue(viewModel.uiState.value.isSaving)
        repository.gate?.complete(Unit)
        withTimeout(5_000) { viewModel.uiState.first { it.saved } }
        val (note, metadata) = repository.saved.single()
        assertEquals(book.copy(title = "Nowy tytuł", author = "Nowy autor", year = null), metadata)
        assertEquals(selectedDate, note.date)
        assertEquals("Book", note.tag)
        assertEquals("Przeczytałam książkę [[${book.canonicalUrl}|Nowy tytuł]] – Nowy autor", note.content)
        assertTrue(config.getLastUpdated() > 0)
    }

    @Test
    fun sameSheetPreservesEditsAndNewSheetResetsSavedState() = runBlocking {
        openAndSelect()
        viewModel.onIntent(BooksIntent.Title("Poprawiony tytuł"))
        viewModel.open("Tytuł", "sheet")
        assertEquals("Poprawiony tytuł", viewModel.uiState.value.selected?.title)
        assertEquals(1, search.queries.size)
        viewModel.onIntent(BooksIntent.Save)
        assertTrue(viewModel.uiState.value.saved)
        viewModel.open("", "new-sheet")
        assertFalse(viewModel.uiState.value.saved)
        assertEquals(null, viewModel.uiState.value.selected)
        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun badMetadataIsRejectedWithoutSaving() = runBlocking {
        openAndSelect()
        viewModel.onIntent(BooksIntent.Year("20x4"))
        viewModel.onIntent(BooksIntent.Save)
        assertNotNull(viewModel.uiState.value.error)
        viewModel.onIntent(BooksIntent.Year("2024"))
        viewModel.onIntent(BooksIntent.Author(" "))
        viewModel.onIntent(BooksIntent.Save)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun searchAndDetailErrorsAreVisibleAndRetryWorks() = runBlocking {
        search.failSearch = true
        viewModel.open("Tytuł", "sheet")
        assertEquals("Błąd wyszukiwania", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        search.failSearch = false
        viewModel.onIntent(BooksIntent.Search)
        assertEquals(listOf(book), viewModel.uiState.value.results)
        search.failDetails = true
        viewModel.onIntent(BooksIntent.Select(book))
        assertEquals("Błąd szczegółów", viewModel.uiState.value.error)
        assertEquals(null, viewModel.uiState.value.selected)
        search.failDetails = false
        viewModel.onIntent(BooksIntent.Select(book))
        assertEquals(book, viewModel.uiState.value.selected)
    }

    @Test
    fun failedSaveKeepsFormAndAllowsRetry() = runBlocking {
        openAndSelect()
        repository.failSave = true
        viewModel.onIntent(BooksIntent.Save)
        assertEquals("Błąd zapisu", viewModel.uiState.value.error)
        assertEquals(book, viewModel.uiState.value.selected)
        assertFalse(viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(0L, config.getLastUpdated())
        repository.failSave = false
        viewModel.onIntent(BooksIntent.Save)
        assertTrue(viewModel.uiState.value.saved)
    }

    @Test
    fun cancellingOldSearchCannotReplaceNewQueryResults() = runBlocking {
        search.waitForSearch = CompletableDeferred()
        viewModel.open("Stary tytuł", "sheet")
        viewModel.onIntent(BooksIntent.Query("Nowy tytuł"))
        search.waitForSearch?.complete(Unit)
        assertEquals("Nowy tytuł", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private inner class FakeSearch : BookSearchRepository {
        val queries = mutableListOf<String>()
        val detailsCalls = mutableListOf<String>()
        var failSearch = false
        var failDetails = false
        var waitForSearch: CompletableDeferred<Unit>? = null
        override suspend fun search(query: String): List<BookMetadata> {
            queries += query
            waitForSearch?.await()
            if (failSearch) throw IOException("Błąd wyszukiwania")
            return listOf(book)
        }
        override suspend fun details(url: String): BookMetadata {
            detailsCalls += url
            if (failDetails) throw IOException("Błąd szczegółów")
            return book
        }
    }

    private class RecordingBooks : BookRepository {
        val saved = mutableListOf<Pair<WikiNote, BookMetadata>>()
        var failSave = false
        var gate: CompletableDeferred<Unit>? = null
        override fun observeBookNotes() = MutableStateFlow<List<BookNoteEntry>>(emptyList())
        override suspend fun saveBookNote(note: WikiNote, metadata: BookMetadata) {
            if (failSave) throw SQLiteException("Błąd zapisu")
            saved += note to metadata
            gate?.await()
        }
    }
}
