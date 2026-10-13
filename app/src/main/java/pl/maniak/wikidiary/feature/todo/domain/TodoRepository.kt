package pl.maniak.wikidiary.feature.todo.domain

import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTodos(): Flow<List<Todo>>

    suspend fun getTodos(): List<Todo>

    suspend fun saveTodo(todo: Todo)

    suspend fun updateTodo(todo: Todo)

    suspend fun deleteTodo(id: Long)
}
