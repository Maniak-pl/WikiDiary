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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import pl.maniak.wikidiary.feature.export.domain.WikiExporter
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Date
import pl.maniak.wikidiary.feature.diary.domain.Category
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.routines.domain.Routine
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.todo.domain.Todo
import pl.maniak.wikidiary.feature.filmweb.domain.FilmNoteEntry
import pl.maniak.wikidiary.feature.pictograms.domain.Pictogram
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.domain.CategoryRepository
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.feature.filmweb.domain.FilmRepository
import pl.maniak.wikidiary.feature.filmweb.domain.FilmwebSearchRepository
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramRepository
import pl.maniak.wikidiary.feature.people.domain.PersonRepository
import pl.maniak.wikidiary.feature.routines.domain.RoutineRepository
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.todo.domain.TodoRepository
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.model.ActionClick.AddCategory
import pl.maniak.wikidiary.ui.model.ActionClick.AddNote
import pl.maniak.wikidiary.ui.model.ActionClick.AddPictogramToTag
import pl.maniak.wikidiary.ui.model.ActionClick.AddPerson
import pl.maniak.wikidiary.ui.model.ActionClick.AddTag
import pl.maniak.wikidiary.ui.model.ActionClick.ConfirmProject
import pl.maniak.wikidiary.ui.model.ActionClick.DataPickerChangeDate
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteCategory
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteNote
import pl.maniak.wikidiary.ui.model.ActionClick.DeletePictogram
import pl.maniak.wikidiary.ui.model.ActionClick.DeletePictogramEntry
import pl.maniak.wikidiary.ui.model.ActionClick.DeletePerson
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteTag
import pl.maniak.wikidiary.ui.model.ActionClick.EditTag
import pl.maniak.wikidiary.ui.model.ActionClick.TagChangeDate
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateCategory
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateFilmweb
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateProject
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreatePictogram
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreatePerson
import pl.maniak.wikidiary.ui.model.ActionClick.UpdateCategory
import pl.maniak.wikidiary.ui.model.BottomSheetUiState
import pl.maniak.wikidiary.ui.model.BottomSheetUiState.CreateProject
import pl.maniak.wikidiary.feature.filmweb.ui.FilmwebSaveState
import pl.maniak.wikidiary.feature.filmweb.ui.FilmwebSearchUiState
import pl.maniak.wikidiary.ui.model.ActionClick.SavePictogram
import pl.maniak.wikidiary.ui.model.ActionClick.SaveWikiPageUrl
import pl.maniak.wikidiary.feature.todo.domain.TodoNoteFactory
import pl.maniak.wikidiary.feature.people.domain.PersonMentionResolver
import pl.maniak.wikidiary.core.wiki.WikiParser

class MainViewModel(
    private val noteRepository: WikiNoteRepository,
    private val tagRepository: TagRepository,
    private val categoryRepository: CategoryRepository,
    private val routineRepository: RoutineRepository,
    private val todoRepository: TodoRepository,
    private val config: Config,
    private val filmRepository: FilmRepository,
    private val filmwebSearchRepository: FilmwebSearchRepository,
    private val pictogramRepository: PictogramRepository,
    private val personRepository: PersonRepository,
    wikiExporter: WikiExporter,
) : ViewModel() {

    val wikiDocument: StateFlow<String> = wikiExporter.document()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _notes = mutableStateOf<List<WikiNote>>(emptyList())
    val notes: State<List<WikiNote>> = _notes

    private val _tags = mutableStateOf<List<Tag>>(emptyList())
    val tags: State<List<Tag>> = _tags

    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    private val _routines = mutableStateOf<List<Routine>>(emptyList())
    val routines: State<List<Routine>> = _routines

    private val _todos = mutableStateOf<List<Todo>>(emptyList())
    val todos: State<List<Todo>> = _todos

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

    private val _pictograms = mutableStateOf<List<Pictogram>>(emptyList())
    val pictograms: State<List<Pictogram>> = _pictograms

    private val _pictogramEntries = mutableStateOf<List<PictogramEntry>>(emptyList())
    val pictogramEntries: State<List<PictogramEntry>> = _pictogramEntries

    private val _pictogramError = mutableStateOf<String?>(null)
    val pictogramError: State<String?> = _pictogramError

    private val _people = mutableStateOf<List<Person>>(emptyList())
    val people: State<List<Person>> = _people

    private val _personError = mutableStateOf<String?>(null)
    val personError: State<String?> = _personError

    private val _wikiPageUrl = mutableStateOf(config.getWikiPageUrl())
    val wikiPageUrl: State<String> = _wikiPageUrl

    private var filmwebSearchJob: Job? = null

    init {
        loadNotes()
        loadTags()
        loadCategories()
        loadRoutines()
        loadTodos()
        loadFilmNotes()
        loadPictograms()
        loadPictogramEntries()
        loadPeople()
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

    private fun loadTodos() {
        viewModelScope.launch {
            _todos.value = todoRepository.getTodos()
        }
    }

    private fun loadPictograms() {
        viewModelScope.launch {
            _pictograms.value = pictogramRepository.getPictograms()
        }
    }

    private fun loadPictogramEntries() {
        viewModelScope.launch {
            _pictogramEntries.value = pictogramRepository.getPictogramEntries()
        }
    }

    private fun loadPeople() {
        viewModelScope.launch {
            _people.value = personRepository.getPeople()
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
            is TagCreatePictogram -> {
                _pictogramError.value = null
                showBottomSheet(BottomSheetUiState.PictogramManager)
            }
            is TagCreatePerson -> {
                _personError.value = null
                showBottomSheet(BottomSheetUiState.PersonManager)
            }
            is AddPerson -> addPerson(action)
            is DeletePerson -> deletePerson(action.id)
            is SavePictogram -> savePictogram(action)
            is DeletePictogram -> deletePictogram(action.id)
            is AddPictogramToTag -> addPictogramToTag(action)
            is DeletePictogramEntry -> deletePictogramEntry(action.id)
            is SaveWikiPageUrl -> saveWikiPageUrl(action.url)
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
            is ActionClick.AddTodo -> addTodo(action.title)
            is ActionClick.DeleteTodo -> deleteTodo(action.id)
            is ActionClick.UpdateTodo -> updateTodo(action.todo)
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
        viewModelScope.launch {
            val people = PersonMentionResolver.resolve(content, _people.value)
            val formattedContent = PersonMentionResolver.replaceWithWikiLinks(content, people)
            noteRepository.saveNoteWithPeople(
                note = WikiNote(
                    tag = tag.name,
                    content = formattedContent,
                    category = tag.category,
                    date = selectedDate.value,
                    people = people
                ),
                personIds = people.map(Person::id)
            )
            config.setLastUpdated(System.currentTimeMillis())
            loadNotes()
        }
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

    private fun savePictogram(action: SavePictogram) {
        val name = action.name.trim()
        val icon = action.icon.trim()
        if (name.isBlank()) {
            _pictogramError.value = "Podaj nazwę pictogramu."
            return
        }
        if (icon.isBlank()) {
            _pictogramError.value = "Wybierz ikonę pictogramu."
            return
        }
        val fileName = WikiParser.pictogramFileName(name)
        val hasFileNameCollision = _pictograms.value.any {
            it.id != action.id && WikiParser.pictogramFileName(it.name) == fileName
        }
        if (hasFileNameCollision) {
            _pictogramError.value = "Ta nazwa generuje już istniejący plik pictogramu."
            return
        }

        viewModelScope.launch {
            try {
                pictogramRepository.savePictogram(
                    Pictogram(id = action.id, name = name, icon = icon)
                )
                _pictogramError.value = null
                _pictograms.value = pictogramRepository.getPictograms()
            } catch (exception: SQLiteException) {
                _pictogramError.value = if (action.id == 0L) {
                    "Pictogram o tej nazwie już istnieje."
                } else {
                    exception.message ?: "Nie udało się zapisać pictogramu."
                }
            }
        }
    }

    private fun deletePictogram(id: Long) {
        viewModelScope.launch {
            pictogramRepository.deletePictogram(id)
            _pictograms.value = pictogramRepository.getPictograms()
            _pictogramEntries.value = pictogramRepository.getPictogramEntries()
        }
    }

    private fun addPictogramToTag(action: AddPictogramToTag) {
        val pictogram = _pictograms.value.firstOrNull { it.id == action.pictogramId }
        if (pictogram == null) {
            _pictogramError.value = "Wybrany pictogram nie jest już dostępny."
            return
        }
        viewModelScope.launch {
            try {
                pictogramRepository.addPictogramEntry(
                    PictogramEntry(
                        pictogramId = pictogram.id,
                        tag = action.tag.name,
                        category = action.tag.category,
                        date = action.date,
                        name = pictogram.name,
                        icon = pictogram.icon
                    )
                )
                _pictogramError.value = null
                _pictogramEntries.value = pictogramRepository.getPictogramEntries()
            } catch (exception: SQLiteException) {
                _pictogramError.value = exception.message
                    ?: "Nie udało się przypisać pictogramu."
            }
        }
    }

    private fun deletePictogramEntry(id: Long) {
        viewModelScope.launch {
            pictogramRepository.deletePictogramEntry(id)
            _pictogramEntries.value = pictogramRepository.getPictogramEntries()
        }
    }

    private fun saveWikiPageUrl(url: String) {
        val normalizedUrl = url.trim()
        config.setWikiPageUrl(normalizedUrl)
        _wikiPageUrl.value = normalizedUrl
    }

    private fun addPerson(action: AddPerson) {
        val normalizedName = action.name.trim()
        if (normalizedName.isBlank()) {
            _personError.value = "Podaj imię i nazwisko."
            return
        }
        if (_people.value.any { it.name.equals(normalizedName, ignoreCase = true) }) {
            _personError.value = "Ta osoba jest już zapisana."
            return
        }
        viewModelScope.launch {
            personRepository.savePerson(Person(name = normalizedName, gender = action.gender))
            _personError.value = null
            loadPeople()
        }
    }

    private fun deletePerson(id: Long) {
        viewModelScope.launch {
            personRepository.deletePerson(id)
            loadPeople()
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

    private fun addTodo(title: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) {
            return
        }

        viewModelScope.launch {
            val todo = Todo(title = normalizedTitle)
            todoRepository.saveTodo(todo)
            noteRepository.saveNote(TodoNoteFactory.createAddedNote(todo, Date()))
            config.setLastUpdated(System.currentTimeMillis())
            _todos.value = todoRepository.getTodos()
            _notes.value = noteRepository.getNotes()
        }
    }

    private fun updateTodo(todo: Todo) {
        val previousTodo = _todos.value.firstOrNull { it.id == todo.id }
        viewModelScope.launch {
            todoRepository.updateTodo(todo)
            if (previousTodo?.isCompleted == false && todo.isCompleted) {
                noteRepository.saveNote(TodoNoteFactory.createCompletedNote(todo, Date()))
                config.setLastUpdated(System.currentTimeMillis())
                _notes.value = noteRepository.getNotes()
            }
            _todos.value = todoRepository.getTodos()
        }
    }

    private fun deleteTodo(id: Long) {
        viewModelScope.launch {
            todoRepository.deleteTodo(id)
            _todos.value = todoRepository.getTodos()
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
