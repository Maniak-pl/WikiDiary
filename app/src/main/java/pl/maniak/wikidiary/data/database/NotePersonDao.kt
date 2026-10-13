package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotePersonDao {
    @Query(
        """
        SELECT person_table.* FROM person_table
        INNER JOIN note_person_table
            ON person_table.id = note_person_table.personId
        WHERE note_person_table.noteId = :noteId
        ORDER BY note_person_table.position
        """
    )
    fun getPeopleForNote(noteId: Long): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<NotePersonEntity>)

    @Query("DELETE FROM note_person_table WHERE noteId = :noteId")
    fun deleteByNoteId(noteId: Long)
}
