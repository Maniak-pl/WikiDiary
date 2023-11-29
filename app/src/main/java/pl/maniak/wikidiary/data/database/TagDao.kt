package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TagDao {
    @Query("SELECT * FROM tag_table")
    fun getAll(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tag: TagEntity)

    @Query("DELETE FROM tag_table WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM tag_table")
    fun deleteAll()
}