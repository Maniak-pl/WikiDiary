package pl.maniak.wikidiary.utils.helpers

import pl.maniak.wikidiary.data.Todo
import pl.maniak.wikidiary.domain.model.WikiNote
import java.util.Date

object TodoNoteFactory {
    const val TODO_TAG = "ToDo"
    const val TODAY_TAG = "Today"

    fun createAddedNote(todo: Todo, date: Date): WikiNote {
        return WikiNote(
            tag = TODO_TAG,
            content = "Dodano zadanie: ${todo.title}",
            date = date
        )
    }

    fun createCompletedNote(todo: Todo, date: Date): WikiNote {
        return WikiNote(
            tag = TODAY_TAG,
            content = "Zakończono zadanie: ${todo.title}",
            date = date
        )
    }
}
