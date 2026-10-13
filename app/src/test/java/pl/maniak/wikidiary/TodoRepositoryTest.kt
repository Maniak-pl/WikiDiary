package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.todo.data.TodoMapper
import pl.maniak.wikidiary.feature.todo.data.TodoRepositoryImpl
import pl.maniak.wikidiary.feature.todo.domain.Todo
import pl.maniak.wikidiary.feature.todo.domain.TodoRepository
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class TodoRepositoryTest {

    private val database = inMemoryDatabase()
    private val repository: TodoRepository = TodoRepositoryImpl(
        mapper = TodoMapper(),
        dao = database.todoDao()
    )

    @After
    fun tearDown() = database.close()

    @Test
    fun todosCanBeCreatedUpdatedAndDeleted() = runBlocking {
        repository.saveTodo(Todo(title = "Prepare release"))
        val created = repository.getTodos().single()

        assertEquals("Prepare release", created.title)
        assertFalse(created.isCompleted)

        repository.updateTodo(created.copy(isCompleted = true))

        assertTrue(repository.getTodos().single().isCompleted)

        repository.deleteTodo(created.id)

        assertTrue(repository.getTodos().isEmpty())
    }

    @Test
    fun observedTodosReflectSavedTodo() = runBlocking {
        val todos = repository.observeTodos().nextValueAfter {
            repository.saveTodo(Todo(title = "Buy milk"))
        }

        assertEquals(listOf("Buy milk"), todos.map(Todo::title))
    }
}
