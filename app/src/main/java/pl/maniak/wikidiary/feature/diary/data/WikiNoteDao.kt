package pl.maniak.wikidiary.feature.diary.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WikiNoteDao {
    @Query("SELECT * FROM wiki_note_table")
    fun getAll(): List<WikiNoteEntity>

    @Query("SELECT COUNT(*) FROM wiki_note_table")
    fun getNoteCount(): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM wiki_note_table
            WHERE tag = :tag AND date >= :startDate AND date < :endDate
        )
        """
    )
    fun hasNoteWithTagBetween(tag: String, startDate: Long, endDate: Long): Boolean

    @Query(
        """
        SELECT wiki_note_table.*,
            person_table.id AS person_id,
            person_table.name AS person_name,
            person_table.gender AS person_gender,
            person_table.isActive AS person_isActive
        FROM wiki_note_table
        LEFT JOIN note_person_table ON note_person_table.noteId = wiki_note_table.id
        LEFT JOIN person_table ON person_table.id = note_person_table.personId
        ORDER BY wiki_note_table.id, note_person_table.position
        """
    )
    fun observeAllWithPeople(): Flow<List<NoteWithPersonRow>>

    @Query("SELECT * FROM wiki_note_table WHERE tag = :tag")
    fun getAllByTag(tag: String): List<WikiNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wikiNote: WikiNoteEntity): Long

    @Query("UPDATE wiki_note_table SET needsCompletion = :needsCompletion WHERE id IN (:ids)")
    fun updateNeedsCompletion(ids: List<Long>, needsCompletion: Boolean)

    @Query("DELETE FROM wiki_note_table WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM wiki_note_table")
    fun deleteAll()
}