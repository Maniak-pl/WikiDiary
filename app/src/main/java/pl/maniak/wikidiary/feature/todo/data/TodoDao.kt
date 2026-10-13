package pl.maniak.wikidiary.feature.todo.data

import androidx.room.Dao
import kotlinx.coroutines.flow.Flow
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_table ORDER BY id")
    fun getAll(): List<TodoEntity>

    @Query("SELECT * FROM todo_table ORDER BY id")
    fun observeAll(): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(todo: TodoEntity): Long

    @Update
    fun update(todo: TodoEntity)

    @Query("DELETE FROM todo_table WHERE id = :id")
    fun deleteById(id: Long)
}
