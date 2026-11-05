package pl.maniak.wikidiary.feature.backup

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BackupDao {
    @Query("SELECT (SELECT COUNT(*) FROM wiki_note_table) + (SELECT COUNT(*) FROM pictogram_entry_table)")
    fun journalEntryCount(): Int

    @Query("DELETE FROM todo_table")
    fun clearTodos()

    @Query("DELETE FROM person_table")
    fun clearPeople()

    @Query("DELETE FROM pictogram_table")
    fun clearPictograms()
}
