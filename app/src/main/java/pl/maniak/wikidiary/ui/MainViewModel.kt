package pl.maniak.wikidiary.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.domain.repository.TagRepository
import pl.maniak.wikidiary.domain.repository.WikiNoteRepository

class MainViewModel(
    private val noteRepository: WikiNoteRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _notes = mutableStateOf<List<WikiNote>>(emptyList())
    val notes: State<List<WikiNote>> = _notes

    private val _tags = mutableStateOf<List<Tag>>(emptyList())
    val tags: State<List<Tag>> = _tags

    init {
        loadNotes()
        loadTags()
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

    fun saveWikiNote(note: WikiNote) {
        viewModelScope.launch {
            noteRepository.saveNote(note)
            loadNotes()
        }
    }

    fun saveTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.saveTag(tag)
            loadTags()
        }
    }
}