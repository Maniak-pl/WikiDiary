package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PictogramDao {
    @Query("SELECT * FROM pictogram_table ORDER BY name")
    fun getAll(): List<PictogramEntity>

    @Query("SELECT * FROM pictogram_table WHERE id = :id")
    fun getById(id: Long): PictogramEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(pictogram: PictogramEntity): Long

    @Update
    fun update(pictogram: PictogramEntity)

    @Query("DELETE FROM pictogram_table WHERE id = :id")
    fun deleteById(id: Long)
}
