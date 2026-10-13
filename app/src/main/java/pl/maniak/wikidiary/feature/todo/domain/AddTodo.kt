package pl.maniak.wikidiary.feature.todo.domain

import java.util.Date
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository

/** Saves a task and logs its creation as a `ToDo` note; blank titles are ignored. */
class AddTodo(
    private val todoRepository: TodoRepository,
    private val noteRepository: WikiNoteRepository,
    private val config: Config,
) {
    suspend operator fun invoke(title: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return
        val todo = Todo(title = normalizedTitle)
        todoRepository.saveTodo(todo)
        noteRepository.saveNote(TodoNoteFactory.createAddedNote(todo, Date()))
        config.setLastUpdated(System.currentTimeMillis())
    }
}
