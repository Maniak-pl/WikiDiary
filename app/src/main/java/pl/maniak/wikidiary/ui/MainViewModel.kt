package pl.maniak.wikidiary.ui

import android.database.sqlite.SQLiteException
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Date
import pl.maniak.wikidiary.data.Category
import pl.maniak.wikidiary.data.Routine
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.domain.repository.CategoryRepository
import pl.maniak.wikidiary.domain.repository.Config
import pl.maniak.wikidiary.domain.repository.FilmRepository
import pl.maniak.wikidiary.domain.repository.FilmwebSearchRepository
import pl.maniak.wikidiary.domain.repository.RoutineRepository
import pl.maniak.wikidiary.domain.repository.TagRepository
import pl.maniak.wikidiary.domain.repository.WikiNoteRepository
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.model.ActionClick.AddCategory
import pl.maniak.wikidiary.ui.model.ActionClick.AddNote
import pl.maniak.wikidiary.ui.model.ActionClick.AddTag
import pl.maniak.wikidiary.ui.model.ActionClick.ConfirmProject
import pl.maniak.wikidiary.ui.model.ActionClick.DataPickerChangeDate
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteCategory
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteNote
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteTag
import pl.maniak.wikidiary.ui.model.ActionClick.EditTag
import pl.maniak.wikidiary.ui.model.ActionClick.TagChangeDate
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateCategory
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateFilmweb
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateProject
import pl.maniak.wikidiary.ui.model.ActionClick.UpdateCategory
import pl.maniak.wikidiary.ui.model.BottomSheetUiState
import pl.maniak.wikidiary.ui.model.BottomSheetUiState.CreateProject
import pl.maniak.wikidiary.ui.model.FilmwebSaveState
import pl.maniak.wikidiary.ui.model.FilmwebSearchUiState
import pl.maniak.wikidiary.utils.helpers.WikiParser

class MainViewModel(
    private val noteRepository: WikiNoteRepository,
    private val tagRepository: TagRepository,
    private val categoryRepository: CategoryRepository,
    private val routineRepository: RoutineRepository,
    private val config: Config,
    private val filmRepository: FilmRepository,
    private val filmwebSearchRepository: FilmwebSearchRepository,
) : ViewModel() {

    private val _notes = mutableStateOf<List<WikiNote>>(emptyList())
    val notes: State<List<WikiNote>> = _notes

    private val _tags = mutableStateOf<List<Tag>>(emptyList())
    val tags: State<List<Tag>> = _tags

    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    private val _routines = mutableStateOf<List<Routine>>(emptyList())
    val routines: State<List<Routine>> = _routines

    private val _filmNotes = mutableStateOf<List<FilmNoteEntry>>(emptyList())
    val filmNotes: State<List<FilmNoteEntry>> = _filmNotes

    private val _bottomSheetExpanded = MutableStateFlow(false)
    val bottomSheetExpanded: StateFlow<Boolean> = _bottomSheetExpanded.asStateFlow()

    private val _bottomSheetUiState = MutableStateFlow<BottomSheetUiState>(BottomSheetUiState.None)
    val bottomSheetUiState: StateFlow<BottomSheetUiState> = _bottomSheetUiState.asStateFlow()

    private val _selectedDate = mutableStateOf(Date())
    val selectedDate: State<Date> = _selectedDate

    private val _showDatePickerDialog = mutableStateOf(false)
    val showDatePickerDialog: State<Boolean> = _showDatePickerDialog

    private val _filmwebSaveState = mutableStateOf<FilmwebSaveState>(FilmwebSaveState.Idle)
    val filmwebSaveState: State<FilmwebSaveState> = _filmwebSaveState

    private val _filmwebSearchState = MutableStateFlow(FilmwebSearchUiState())
    val filmwebSearchState: StateFlow<FilmwebSearchUiState> = _filmwebSearchState.asStateFlow()

    private var filmwebSearchJob: Job? = null

    init {
        loadNotes()
        loadTags()
        loadCategories()
        loadRoutines()
        loadFilmNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _notes.value = noteRepository.getNotes()
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            _tags.value = tagRepository.getTags()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = categoryRepository.getCategories()
        }
    }

    private fun loadRoutines() {
        if (config.isFirstLaunchToday()) {
            viewModelScope.launch {
                val allRoutines = routineRepository.getRoutines()
                val updatedRoutines = allRoutines.map { it.copy(isCompleted = false) }
                routineRepository.updateAll(updatedRoutines)
                _routines.value = updatedRoutines
            }
        } else {
            viewModelScope.launch {
                _routines.value = routineRepository.getRoutines()
            }
        }
    }

    private fun loadFilmNotes() {
        viewModelScope.launch {
            _filmNotes.value = filmRepository.getFilmNotes()
        }
    }

    fun onActionClick(action: ActionClick) {
        when (action) {
            is AddNote -> saveNoteAndUpdateTag(action.tag, action.content)
            is AddTag -> saveTag(Tag(id = 0, name = action.tag))
            is DeleteNote -> deleteNote(note = action.note)
            is DeleteTag -> {
                deleteTag(id = action.id)
                hideBottomSheet()
            }

            is EditTag -> showBottomSheet(CreateProject(action.tag))
            is TagCreateProject -> showBottomSheet(CreateProject(null))
            is TagCreateFilmweb -> {
                _filmwebSaveState.value = FilmwebSaveState.Idle
                resetFilmwebSearch()
                showBottomSheet(BottomSheetUiState.FilmwebSearch)
            }
            is ConfirmProject -> {
                saveTag(
                    Tag(
                        id = action.id,
                        name = action.name,
                        category = action.category,
                        color = action.color,
                        date = Date()
                    )
                )
                hideBottomSheet()
            }

            is TagCreateCategory -> showBottomSheet(BottomSheetUiState.CreateCategory)
            is TagChangeDate -> _showDatePickerDialog.value = true
            is AddCategory -> saveCategory(Category(id = 0, name = action.name))
            is DeleteCategory -> deleteCategory(action.id)
            is UpdateCategory -> saveCategory(Category(id = action.id, name = action.name))
            is ActionClick.UpdateRoutine -> {
                if (action.routine.isCompleted) {
                    saveWikiNote(
                        tag = "Routine",
                        content = "Wykonano rutynę \"${action.routine.name}\"",
                    )
                }
                saveRoutine(action.routine)
            }

            is ActionClick.AddRoutine -> saveRoutine(Routine(name = action.name))
            is ActionClick.DeleteRoutine -> deleteRoutine(action.id)
            is DataPickerChangeDate -> {
                _selectedDate.value = action.date
                _showDatePickerDialog.value = false
            }

            is ActionClick.SaveFilmNote -> saveFilmNote(action)
            ActionClick.CloseBottomSheet -> hideBottomSheet()
            is ActionClick.EditNote -> Unit
            is ActionClick.DeleteNotes -> {
                viewModelScope.launch {
                    action.notes.forEach { id ->
                        noteRepository.deleteNoteById(id)
                    }
                    _notes.value = noteRepository.getNotes()
                    _filmNotes.value = filmRepository.getFilmNotes()
                }
            }
        }
    }

    private fun saveFilmNote(action: ActionClick.SaveFilmNote) {
        _filmwebSaveState.value = FilmwebSaveState.Saving
        viewModelScope.launch {
            try {
                val note = WikiNote(
                    tag = action.tag.name,
                    content = WikiParser.addFilmNote(action.sentencePrefix, action.metadata),
                    category = action.tag.category,
                    date = action.date
                )
                filmRepository.saveFilmNote(note, action.metadata)
                tagRepository.saveTag(action.tag.copy(date = Date()))
                config.setLastUpdated(System.currentTimeMillis())
                loadNotes()
                loadFilmNotes()
                loadTags()
                _filmwebSaveState.value = FilmwebSaveState.Idle
                hideBottomSheet()
            } catch (exception: SQLiteException) {
                _filmwebSaveState.value = FilmwebSaveState.Error(
                    exception.message ?: "Nie udało się zapisać notatki."
                )
            } catch (exception: IllegalStateException) {
                _filmwebSaveState.value = FilmwebSaveState.Error(
                    exception.message ?: "Nie udało się zapisać metadanych filmu."
                )
            }
        }
    }

    fun setFilmwebSearchQuery(query: String) {
        _filmwebSearchState.value = _filmwebSearchState.value.copy(query = query)
    }

    fun searchFilmweb() {
        val query = _filmwebSearchState.value.query.trim()
        if (query.isBlank()) {
            _filmwebSearchState.value = _filmwebSearchState.value.copy(
                error = "Wpisz tytuł filmu lub serialu.",
                hasSearched = false
            )
            return
        }

        filmwebSearchJob?.cancel()
        _filmwebSearchState.value = _filmwebSearchState.value.copy(
            query = query,
            isLoading = true,
            results = emptyList(),
            error = null,
            hasSearched = true
        )
        filmwebSearchJob = viewModelScope.launch {
            try {
                val results = filmwebSearchRepository.search(query)
                _filmwebSearchState.value = _filmwebSearchState.value.copy(
                    isLoading = false,
                    results = results,
                    error = null
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                _filmwebSearchState.value = _filmwebSearchState.value.copy(
                    isLoading = false,
                    error = exception.message
                        ?: "Nie udało się pobrać wyników Filmweb. Sprawdź połączenie z internetem."
                )
            }
        }
    }

    private fun resetFilmwebSearch() {
        filmwebSearchJob?.cancel()
        _filmwebSearchState.value = FilmwebSearchUiState()
    }

    private fun saveNoteAndUpdateTag(tag: Tag, content: String) {
        saveWikiNote(tag.name, content, tag.category)
        saveTag(tag.copy(date = Date()))
    }

    private fun saveWikiNote(tag: String, content: String, category: String? = null) {
        viewModelScope.launch {
            noteRepository.saveNote(
                WikiNote(
                    tag = tag,
                    content = content,
                    category = category,
                    date = selectedDate.value
                )
            )
            config.setLastUpdated(System.currentTimeMillis())
            loadNotes()
        }
    }

    private fun saveTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.saveTag(tag)
            loadTags()
        }
    }

    private fun deleteNote(note: WikiNote) {
        viewModelScope.launch {
            noteRepository.deleteNoteById(note.id)
            _notes.value = noteRepository.getNotes()
            _filmNotes.value = filmRepository.getFilmNotes()
        }
    }

    private fun deleteTag(id: Long) {
        viewModelScope.launch {
            tagRepository.deleteTag(id)
            loadTags()
        }
    }

    private fun saveCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.saveCategory(category)
            loadCategories()
        }
    }

    private fun deleteCategory(id: Long) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id)
            loadCategories()
        }
    }

    private fun saveRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.saveRoutine(routine)
            loadRoutines()
        }
    }

    private fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(id)
            loadRoutines()
        }
    }

    private fun showBottomSheet(uiState: BottomSheetUiState) {
        _bottomSheetUiState.value = uiState
        _bottomSheetExpanded.value = true
    }

    private fun hideBottomSheet() {
        _bottomSheetExpanded.value = false
        _bottomSheetUiState.value = BottomSheetUiState.None
    }

    fun setBottomSheetExpanded(isExpanded: Boolean) {
        _bottomSheetExpanded.value = isExpanded
    }
}
