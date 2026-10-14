package pl.maniak.wikidiary.feature.diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramRepository

data class NotesListUiState(
    val notes: List<WikiNote> = emptyList(),
    val pictogramEntries: List<PictogramEntry> = emptyList(),
)

sealed interface NotesListIntent {
    data class DeleteNotes(val ids: List<Long>) : NotesListIntent
    data class SetNeedsCompletion(val ids: List<Long>, val needsCompletion: Boolean) : NotesListIntent
    data class DeletePictogramEntry(val id: Long) : NotesListIntent
}

class NotesListViewModel(
    private val noteRepository: WikiNoteRepository,
    private val pictogramRepository: PictogramRepository,
) : ViewModel() {

    val uiState: StateFlow<NotesListUiState> = combine(
        noteRepository.observeNotes(),
        pictogramRepository.observePictogramEntries(),
        ::NotesListUiState
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesListUiState())

    fun onIntent(intent: NotesListIntent) {
        viewModelScope.launch {
            when (intent) {
                is NotesListIntent.DeleteNotes -> intent.ids.forEach { noteRepository.deleteNoteById(it) }
                is NotesListIntent.SetNeedsCompletion ->
                    noteRepository.setNeedsCompletion(intent.ids, intent.needsCompletion)
                is NotesListIntent.DeletePictogramEntry -> pictogramRepository.deletePictogramEntry(intent.id)
            }
        }
    }
}
