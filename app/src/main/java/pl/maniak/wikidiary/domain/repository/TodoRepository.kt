package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.data.Todo

interface TodoRepository {
    suspend fun getTodos(): List<Todo>

    suspend fun saveTodo(todo: Todo)

    suspend fun updateTodo(todo: Todo)

    suspend fun deleteTodo(id: Long)
}
