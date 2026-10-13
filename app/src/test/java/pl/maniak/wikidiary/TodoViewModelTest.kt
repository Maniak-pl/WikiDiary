package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.todo.domain.Todo
import pl.maniak.wikidiary.feature.todo.ui.TodoIntent
import pl.maniak.wikidiary.feature.todo.ui.TodoViewModel
import pl.maniak.wikidiary.testing.InMemoryConfig
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class TodoViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val viewModel by lazy {
        TodoViewModel(repositories.todos, repositories.notes, InMemoryConfig())
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun addingTaskWritesNoteUnderToDoTag() = runBlocking {
        val notes = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(TodoIntent.Add("Buy milk"))
        }

        assertEquals(listOf("ToDo" to "Dodano zadanie: Buy milk"), notes.map { it.tag to it.content })
    }

    @Test
    fun completingTaskWritesNoteUnderTodayTag() = runBlocking {
        repositories.todos.saveTodo(Todo(title = "Buy milk"))
        val todo = repositories.todos.getTodos().single()

        val notes = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(TodoIntent.SetCompleted(todo.id, completed = true))
        }

        assertEquals(listOf("Today" to "Zakończono zadanie: Buy milk"), notes.map { it.tag to it.content })
    }
}
