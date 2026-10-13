package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.feature.todo.domain.Todo
import pl.maniak.wikidiary.feature.todo.data.TodoDao
import pl.maniak.wikidiary.feature.todo.data.TodoEntity
import pl.maniak.wikidiary.feature.todo.data.TodoMapper
import pl.maniak.wikidiary.feature.todo.domain.TodoRepository
import pl.maniak.wikidiary.feature.todo.data.TodoRepositoryImpl

class TodoRepositoryTest {

    @Test
    fun todosCanBeCreatedUpdatedAndDeleted() = runBlocking {
        val dao = InMemoryTodoDao()
        val repository: TodoRepository = TodoRepositoryImpl(
            mapper = TodoMapper(),
            dao = dao
        )

        repository.saveTodo(Todo(title = "Prepare release"))
        val created = repository.getTodos().single()

        assertEquals("Prepare release", created.title)
        assertFalse(created.isCompleted)

        repository.updateTodo(created.copy(isCompleted = true))

        assertTrue(repository.getTodos().single().isCompleted)

        repository.deleteTodo(created.id)

        assertTrue(repository.getTodos().isEmpty())
    }
}

private class InMemoryTodoDao : TodoDao {
    private val values = linkedMapOf<Long, TodoEntity>()
    private var nextId = 1L

    override fun getAll(): List<TodoEntity> = values.values.toList()

    override fun insert(todo: TodoEntity): Long {
        val id = if (todo.id == 0L) nextId++ else todo.id
        values[id] = todo.copy(id = id)
        return id
    }

    override fun update(todo: TodoEntity) {
        values[todo.id] = todo
    }

    override fun deleteById(id: Long) {
        values.remove(id)
    }
}
