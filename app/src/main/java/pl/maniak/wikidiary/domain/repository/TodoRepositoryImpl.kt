package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.Todo
import pl.maniak.wikidiary.data.database.TodoDao
import pl.maniak.wikidiary.data.mapper.TodoMapper

class TodoRepositoryImpl(
    private val mapper: TodoMapper,
    private val dao: TodoDao
) : TodoRepository {
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
