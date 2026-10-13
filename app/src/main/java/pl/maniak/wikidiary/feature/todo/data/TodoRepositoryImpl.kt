package pl.maniak.wikidiary.feature.todo.data

import pl.maniak.wikidiary.feature.todo.domain.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.todo.domain.Todo

class TodoRepositoryImpl(
    private val mapper: TodoMapper,
    private val dao: TodoDao
) : TodoRepository {
    override fun observeTodos(): Flow<List<Todo>> =
        dao.observeAll().map { todos -> todos.map(mapper::mapToDomain) }

    override suspend fun getTodos(): List<Todo> = withContext(Dispatchers.IO) {
        dao.getAll().map(mapper::mapToDomain)
    }

    override suspend fun saveTodo(todo: Todo) = withContext(Dispatchers.IO) {
        dao.insert(mapper.mapToEntity(todo))
        Unit
    }

    override suspend fun updateTodo(todo: Todo) = withContext(Dispatchers.IO) {
        dao.update(mapper.mapToEntity(todo))
    }

    override suspend fun deleteTodo(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
}
