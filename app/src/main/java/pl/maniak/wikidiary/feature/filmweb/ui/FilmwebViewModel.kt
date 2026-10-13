package pl.maniak.wikidiary.feature.filmweb.ui

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.filmweb.domain.FilmMetadata
import pl.maniak.wikidiary.feature.filmweb.domain.FilmRepository
import pl.maniak.wikidiary.feature.filmweb.domain.FilmwebSearchRepository

data class FilmwebUiState(
    val search: FilmwebSearchUiState = FilmwebSearchUiState(),
    val save: FilmwebSaveState = FilmwebSaveState.Idle,
    val tags: List<Tag> = emptyList(),
    val selectedDate: Date = Date(),
)

sealed interface FilmwebIntent {
    data class ChangeQuery(val query: String) : FilmwebIntent
    data object Search : FilmwebIntent
    data class Save(
        val tag: Tag,
        val date: Date,
        val metadata: FilmMetadata,
        val sentencePrefix: String,
    ) : FilmwebIntent
    data object Reset : FilmwebIntent
}

class FilmwebViewModel(
    private val filmRepository: FilmRepository,
    private val searchRepository: FilmwebSearchRepository,
    private val tagRepository: TagRepository,
    private val config: Config,
    selectedDate: SelectedDateStore,
) : ViewModel() {

    private val search = MutableStateFlow(FilmwebSearchUiState())
    private val save = MutableStateFlow<FilmwebSaveState>(FilmwebSaveState.Idle)
    private var searchJob: Job? = null

    val uiState: StateFlow<FilmwebUiState> = combine(
        search,
        save,
        tagRepository.observeTags(),
        selectedDate.date,
        ::FilmwebUiState
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FilmwebUiState(selectedDate = selectedDate.date.value)
    )

    fun onIntent(intent: FilmwebIntent) {
        when (intent) {
            is FilmwebIntent.ChangeQuery -> search.update { it.copy(query = intent.query) }
            FilmwebIntent.Search -> search()
            is FilmwebIntent.Save -> save(intent)
            FilmwebIntent.Reset -> {
                searchJob?.cancel()
                search.value = FilmwebSearchUiState()
                save.value = FilmwebSaveState.Idle
            }
        }
    }

    private fun search() {
        val query = search.value.query.trim()
        if (query.isBlank()) {
            search.update { it.copy(error = "Wpisz tytuł filmu lub serialu.", hasSearched = false) }
            return
        }
        searchJob?.cancel()
        search.update {
            it.copy(query = query, isLoading = true, results = emptyList(), error = null, hasSearched = true)
        }
        searchJob = viewModelScope.launch {
            try {
                val results = searchRepository.search(query)
                search.update { it.copy(isLoading = false, results = results, error = null) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                search.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message
                            ?: "Nie udało się pobrać wyników Filmweb. Sprawdź połączenie z internetem."
                    )
                }
            }
        }
    }

    private fun save(intent: FilmwebIntent.Save) {
        save.value = FilmwebSaveState.Saving
        viewModelScope.launch {
            try {
                filmRepository.saveFilmNote(
                    WikiNote(
                        tag = intent.tag.name,
                        content = WikiParser.addFilmNote(intent.sentencePrefix, intent.metadata),
                        category = intent.tag.category,
                        date = intent.date
                    ),
                    intent.metadata
                )
                tagRepository.saveTag(intent.tag.copy(date = Date()))
                config.setLastUpdated(System.currentTimeMillis())
                save.value = FilmwebSaveState.Saved
            } catch (exception: SQLiteException) {
                save.value = FilmwebSaveState.Error(exception.message ?: "Nie udało się zapisać notatki.")
            } catch (exception: IllegalStateException) {
                save.value = FilmwebSaveState.Error(
                    exception.message ?: "Nie udało się zapisać metadanych filmu."
                )
            }
        }
    }
}
