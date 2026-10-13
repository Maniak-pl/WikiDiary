package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.maniak.wikidiary.data.Todo
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.TodoNoteFactory
import java.util.Date

class TodoNoteFactoryTest {

    @Test
    fun createsDiaryNotesForAddingAndCompletingATask() {
        val task = Todo(id = 7, title = "Prepare release")
        val eventDate = Date(1_728_480_000_000)

        assertEquals(
            WikiNote(
                tag = "ToDo",
                content = "Dodano zadanie: Prepare release",
                date = eventDate
            ),
            TodoNoteFactory.createAddedNote(task, eventDate)
        )
        assertEquals(
            WikiNote(
                tag = "Today",
                content = "Zakończono zadanie: Prepare release",
                date = eventDate
            ),
            TodoNoteFactory.createCompletedNote(task, eventDate)
        )
    }
}
