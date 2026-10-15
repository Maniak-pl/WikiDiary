package pl.maniak.wikidiary.feature.books.ui

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.books.domain.BookMetadata
import pl.maniak.wikidiary.feature.books.domain.BookRepository
import pl.maniak.wikidiary.feature.books.domain.BookSearchRepository
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.diary.domain.WikiNote

data class BooksUiState(
    val query: String = "",
    val results: List<BookMetadata> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val selected: BookMetadata? = null,
    val year: String = "",
    val prefix: String = "Przeczytałem",
    val tags: List<Tag> = emptyList(),
    val tag: Tag? = null,
    val date: Date = Date(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

sealed interface BooksIntent {
    data class Query(val value: String) : BooksIntent
    data object Search : BooksIntent
    data class Select(val book: BookMetadata) : BooksIntent
    data object BackToResults : BooksIntent
    data class Title(val value: String) : BooksIntent
    data class Author(val value: String) : BooksIntent
    data class Year(val value: String) : BooksIntent
    data class Prefix(val value: String) : BooksIntent
    data class TagSelected(val tag: Tag) : BooksIntent
    data object Save : BooksIntent
}

class BooksViewModel(
    private val search: BookSearchRepository,
    private val repository: BookRepository,
    tags: TagRepository,
    selectedDate: SelectedDateStore,
    private val config: Config,
) : ViewModel() {
    private val state = MutableStateFlow(BooksUiState(date = selectedDate.date.value))
    val uiState: StateFlow<BooksUiState> = state
    private var request: Job? = null
    private var saveJob: Job? = null
    private var sessionId: String? = null

    init {
        viewModelScope.launch {
            tags.observeTags().collect { tags ->
                state.update { current ->
                    current.copy(tags = tags, tag = tags.firstOrNull { it.id == current.tag?.id }
                        ?: tags.firstOrNull { it.name.equals("Book", ignoreCase = true) })
                }
            }
        }
        viewModelScope.launch {
            selectedDate.date.collect { date -> state.update { it.copy(date = date) } }
        }
    }

    suspend fun open(query: String, sessionId: String) {
        if (this.sessionId == sessionId) return
        saveJob?.join()
        request?.cancel()
        this.sessionId = sessionId
        val old = state.value
        state.value = BooksUiState(
            query = query,
            tags = old.tags,
            tag = old.tags.firstOrNull { it.name.equals("Book", ignoreCase = true) },
            date = old.date
        )
        if (query.isNotBlank()) search()
    }

    fun onIntent(intent: BooksIntent) {
        if (state.value.isSaving || state.value.saved) return
        when (intent) {
            is BooksIntent.Query -> {
                request?.cancel()
                state.update {
                    it.copy(query = intent.value, selected = null, results = emptyList(),
                        hasSearched = false, isLoading = false, error = null)
                }
            }
            BooksIntent.Search -> search()
            is BooksIntent.Select -> select(intent.book)
            BooksIntent.BackToResults -> {
                request?.cancel()
                state.update { it.copy(selected = null, isLoading = false, error = null) }
            }
            is BooksIntent.Title -> state.update {
                it.copy(selected = it.selected?.copy(title = intent.value), error = null)
            }
            is BooksIntent.Author -> state.update {
                it.copy(selected = it.selected?.copy(author = intent.value), error = null)
            }
            is BooksIntent.Year -> state.update { it.copy(year = intent.value, error = null) }
            is BooksIntent.Prefix -> state.update { it.copy(prefix = intent.value, error = null) }
            is BooksIntent.TagSelected -> state.update { it.copy(tag = intent.tag, error = null) }
            BooksIntent.Save -> save()
        }
    }

    private fun search() {
        val query = state.value.query.trim()
        if (query.isBlank()) {
            state.update { it.copy(error = "Wpisz tytuł książki lub autora.") }
            return
        }
        request?.cancel()
        state.update { it.copy(isLoading = true, selected = null, results = emptyList(), error = null, hasSearched = false) }
        request = viewModelScope.launch {
            try {
                val results = search.search(query)
                ensureActive()
                state.update { it.copy(isLoading = false, results = results, hasSearched = true) }
            } catch (exception: IOException) {
                state.update { it.copy(isLoading = false, error = exception.message ?: "Nie udało się wyszukać książek.") }
            }
        }
    }

    private fun select(book: BookMetadata) {
        request?.cancel()
        state.update { it.copy(isLoading = true, error = null) }
        request = viewModelScope.launch {
            try {
                val details = search.details(book.canonicalUrl)
                ensureActive()
                state.update { it.copy(isLoading = false, selected = details, year = details.year?.toString().orEmpty()) }
            } catch (exception: IOException) {
                state.update {
                    it.copy(isLoading = false, error = exception.message ?: "Nie udało się pobrać danych książki.")
                }
            }
        }
    }

    private fun save() {
        val form = state.value
        val book = form.selected?.copy(
            title = form.selected.title.trim(),
            author = form.selected.author.trim(),
            year = form.year.toIntOrNull()
        )
        if (form.isLoading || book == null || !book.isValid() ||
            (form.year.isNotBlank() && !Regex("[0-9]{4}").matches(form.year)) || form.tag == null
        ) {
            state.update { it.copy(error = "Wybierz książkę i tag. Uzupełnij tytuł, autora i poprawny rok lub pozostaw rok pusty.") }
            return
        }
        state.update { it.copy(isSaving = true, error = null) }
        saveJob = viewModelScope.launch {
            try {
                repository.saveBookNote(
                    WikiNote(
                        tag = form.tag.name,
                        category = form.tag.category,
                        content = WikiParser.addBookNote(form.prefix, book),
                        date = form.date
                    ),
                    book
                )
                config.setLastUpdated(System.currentTimeMillis())
                state.update { it.copy(isSaving = false, saved = true) }
            } catch (exception: SQLiteException) {
                state.update { it.copy(isSaving = false, error = exception.message ?: "Nie udało się zapisać książki.") }
            }
        }
    }
}
