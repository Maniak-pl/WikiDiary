package pl.maniak.wikidiary.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.data.Category
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.domain.repository.CategoryRepository
import pl.maniak.wikidiary.domain.repository.TagRepository
import pl.maniak.wikidiary.domain.repository.WikiNoteRepository
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.model.ActionClick.AddCategory
import pl.maniak.wikidiary.ui.model.ActionClick.AddNote
import pl.maniak.wikidiary.ui.model.ActionClick.AddTag
import pl.maniak.wikidiary.ui.model.ActionClick.ConfirmCreateProject
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteCategory
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteNote
import pl.maniak.wikidiary.ui.model.ActionClick.DeleteTag
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateCategory
import pl.maniak.wikidiary.ui.model.ActionClick.TagCreateProject
import pl.maniak.wikidiary.ui.model.ActionClick.UpdateCategory
import pl.maniak.wikidiary.ui.model.BottomSheetUiState

class MainViewModel(
    private val noteRepository: WikiNoteRepository,
    private val tagRepository: TagRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _notes = mutableStateOf<List<WikiNote>>(emptyList())
    val notes: State<List<WikiNote>> = _notes

    private val _tags = mutableStateOf<List<Tag>>(emptyList())
    val tags: State<List<Tag>> = _tags

    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    private val _bottomSheetExpanded = MutableStateFlow(false)
    val bottomSheetExpanded: StateFlow<Boolean> = _bottomSheetExpanded.asStateFlow()

    private val _bottomSheetUiState = MutableStateFlow<BottomSheetUiState>(BottomSheetUiState.None)
    val bottomSheetUiState: StateFlow<BottomSheetUiState> = _bottomSheetUiState.asStateFlow()

    init {
        loadNotes()
        loadTags()
        loadCategories()
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

    fun onActionClick(action: ActionClick) {
        when (action) {
            is AddNote -> saveWikiNote(note = action.note)
            is AddTag -> saveTag(tag = action.tag)
            is DeleteNote -> deleteNote(note = action.note)
            is DeleteTag -> deleteTag(tag = action.tag)
            is TagCreateProject -> showBottomSheet(BottomSheetUiState.CreateProject)
            is ConfirmCreateProject -> {
                saveTag(Tag(id = 0, tag = action.name, folder = action.category))
                hideBottomSheet()
            }
            is TagCreateCategory -> showBottomSheet(BottomSheetUiState.CreateCategory)
            is AddCategory -> saveCategory(Category(id = 0, name = action.name))
            is DeleteCategory -> deleteCategory(action.id)
            is UpdateCategory -> saveCategory(Category(id = action.id, name = action.name))
        }
    }

    private fun saveWikiNote(note: WikiNote) {
        viewModelScope.launch {
            noteRepository.saveNote(note)
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
            loadNotes()
        }
    }

    private fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.deleteTag(tag.id)
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