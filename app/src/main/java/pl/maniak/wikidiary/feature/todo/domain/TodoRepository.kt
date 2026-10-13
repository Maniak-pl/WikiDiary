package pl.maniak.wikidiary.feature.todo.domain


interface TodoRepository {
    suspend fun getTodos(): List<Todo>

    suspend fun saveTodo(todo: Todo)

    suspend fun updateTodo(todo: Todo)

    suspend fun deleteTodo(id: Long)
}
