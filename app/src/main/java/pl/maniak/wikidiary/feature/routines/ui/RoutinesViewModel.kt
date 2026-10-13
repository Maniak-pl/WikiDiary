package pl.maniak.wikidiary.feature.routines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.routines.domain.Routine
import pl.maniak.wikidiary.feature.routines.domain.RoutineRepository

data class RoutinesUiState(val routines: List<Routine> = emptyList())

sealed interface RoutinesIntent {
    data class Add(val name: String) : RoutinesIntent
    data class Delete(val id: Long) : RoutinesIntent
    data class SetCompleted(val id: Long, val completed: Boolean) : RoutinesIntent
}

class RoutinesViewModel(
    private val routineRepository: RoutineRepository,
    private val noteRepository: WikiNoteRepository,
    private val config: Config,
    private val selectedDate: SelectedDateStore,
) : ViewModel() {

    val uiState: StateFlow<RoutinesUiState> = routineRepository.observeRoutines()
        .map(::RoutinesUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoutinesUiState())

    init {
        if (config.isFirstLaunchToday()) {
            viewModelScope.launch {
                routineRepository.updateAll(
                    routineRepository.getRoutines().map { it.copy(isCompleted = false) }
                )
            }
        }
    }

    fun onIntent(intent: RoutinesIntent) {
        when (intent) {
            is RoutinesIntent.Add -> viewModelScope.launch {
                routineRepository.saveRoutine(Routine(name = intent.name))
            }
            is RoutinesIntent.Delete -> viewModelScope.launch {
                routineRepository.deleteRoutine(intent.id)
            }
            is RoutinesIntent.SetCompleted -> setCompleted(intent.id, intent.completed)
        }
    }

    private fun setCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            val routine = routineRepository.getRoutines().firstOrNull { it.id == id } ?: return@launch
            if (completed) {
                noteRepository.saveNote(
                    WikiNote(
                        tag = ROUTINE_TAG,
                        content = "Wykonano rutynę \"${routine.name}\"",
                        date = selectedDate.date.value
                    )
                )
                config.setLastUpdated(System.currentTimeMillis())
            }
            routineRepository.saveRoutine(routine.copy(isCompleted = completed))
        }
    }

    private companion object {
        const val ROUTINE_TAG = "Routine"
    }
}
