package pl.maniak.wikidiary.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.domain.repository.WikiNoteRepository

class MainViewModel(private val repository: WikiNoteRepository) : ViewModel() {

    private val _notes = mutableStateOf<List<WikiNote>>(emptyList())
    val notes: State<List<WikiNote>> = _notes

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _notes.value = repository.getNotes()
        }
    }

    fun saveWikiNote(note: WikiNote) {
        viewModelScope.launch {
            repository.saveNote(note)
            loadNotes()
        }
    }
}