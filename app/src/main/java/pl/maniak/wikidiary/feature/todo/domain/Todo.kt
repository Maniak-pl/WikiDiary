package pl.maniak.wikidiary.feature.todo.domain

data class Todo(
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false
)
