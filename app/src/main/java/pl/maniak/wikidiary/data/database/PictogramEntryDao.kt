package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PictogramEntryDao {
    @Query(
        """
        SELECT
            entry.id AS id,
            entry.pictogramId AS pictogramId,
            entry.tag AS tag,
            entry.category AS category,
            entry.date AS date,
            pictogram.name AS name,
            pictogram.icon AS icon
        FROM pictogram_entry_table AS entry
        INNER JOIN pictogram_table AS pictogram
            ON pictogram.id = entry.pictogramId
        ORDER BY entry.date, entry.id
        """
    )
    fun getAll(): List<PictogramEntryRecord>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(entry: PictogramEntryEntity): Long

    @Query("DELETE FROM pictogram_entry_table WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM pictogram_entry_table WHERE pictogramId = :pictogramId")
    fun deleteByPictogramId(pictogramId: Long)
}
