package pl.maniak.wikidiary.feature.todo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.todo.domain.Todo
import pl.maniak.wikidiary.feature.todo.domain.TodoNoteFactory
import pl.maniak.wikidiary.feature.todo.domain.TodoRepository

data class TodoUiState(val todos: List<Todo> = emptyList())

sealed interface TodoIntent {
    data class Add(val title: String) : TodoIntent
    data class Delete(val id: Long) : TodoIntent
    data class SetCompleted(val id: Long, val completed: Boolean) : TodoIntent
}

class TodoViewModel(
    private val todoRepository: TodoRepository,
    private val noteRepository: WikiNoteRepository,
    private val config: Config,
) : ViewModel() {

    val uiState: StateFlow<TodoUiState> = todoRepository.observeTodos()
        .map(::TodoUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoUiState())

    fun onIntent(intent: TodoIntent) {
        when (intent) {
            is TodoIntent.Add -> add(intent.title)
            is TodoIntent.Delete -> viewModelScope.launch { todoRepository.deleteTodo(intent.id) }
            is TodoIntent.SetCompleted -> setCompleted(intent.id, intent.completed)
        }
    }

    private fun add(title: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return
        viewModelScope.launch {
            val todo = Todo(title = normalizedTitle)
            todoRepository.saveTodo(todo)
            noteRepository.saveNote(TodoNoteFactory.createAddedNote(todo, Date()))
            config.setLastUpdated(System.currentTimeMillis())
        }
    }

    private fun setCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            val todo = todoRepository.getTodos().firstOrNull { it.id == id } ?: return@launch
            val updated = todo.copy(isCompleted = completed)
            todoRepository.updateTodo(updated)
            if (!todo.isCompleted && completed) {
                noteRepository.saveNote(TodoNoteFactory.createCompletedNote(updated, Date()))
                config.setLastUpdated(System.currentTimeMillis())
            }
        }
    }
}
