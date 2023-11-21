package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WikiNoteDao {
    @Query("SELECT * FROM wiki_note_table")
    fun getAll(): List<WikiNoteEntity>

    @Query("SELECT * FROM wiki_note_table WHERE tag = :tag")
    fun getAllByTag(tag: String): List<WikiNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wikiNote: WikiNoteEntity)

    @Query("DELETE FROM wiki_note_table WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM wiki_note_table")
    fun deleteAll()
}