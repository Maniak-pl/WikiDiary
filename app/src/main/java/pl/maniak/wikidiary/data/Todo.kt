package pl.maniak.wikidiary.data

data class Todo(
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false
)
