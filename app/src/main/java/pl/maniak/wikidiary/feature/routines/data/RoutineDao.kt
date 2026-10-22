package pl.maniak.wikidiary.feature.routines.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routine: RoutineEntity)

    @Query(
        """
        SELECT * FROM routines
        ORDER BY (
            SELECT COUNT(*) FROM wiki_note_table
            WHERE tag = 'Routine'
                AND content = 'Wykonano rutynę "' || routines.name || '"'
        ) DESC, name COLLATE NOCASE
        """
    )
    fun getAllRoutines(): List<RoutineEntity>

    @Query(
        """
        SELECT * FROM routines
        ORDER BY (
            SELECT COUNT(*) FROM wiki_note_table
            WHERE tag = 'Routine'
                AND content = 'Wykonano rutynę "' || routines.name || '"'
        ) DESC, name COLLATE NOCASE
        """
    )
    fun observeAll(): Flow<List<RoutineEntity>>

    @Query("DELETE FROM routines WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM routines")
    fun deleteAll()

    @Update
    fun updateAll(map: List<RoutineEntity>)
}