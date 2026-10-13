package pl.maniak.wikidiary.feature.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false
)
