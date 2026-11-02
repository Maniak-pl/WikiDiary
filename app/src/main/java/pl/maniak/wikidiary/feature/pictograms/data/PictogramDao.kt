package pl.maniak.wikidiary.feature.pictograms.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.pictograms.domain.documentPictogram

@Dao
interface PictogramDao {
    @Query("SELECT * FROM pictogram_table ORDER BY name")
    fun getAll(): List<PictogramEntity>

    @Query("SELECT * FROM pictogram_table ORDER BY name")
    fun observeAll(): Flow<List<PictogramEntity>>

    @Query("SELECT * FROM pictogram_table WHERE id = :id")
    fun getById(id: Long): PictogramEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(pictogram: PictogramEntity): Long

    @Update
    fun update(pictogram: PictogramEntity)

    @Query("DELETE FROM pictogram_table WHERE id = :id")
    fun deleteById(id: Long)

    fun ensureDocumentPictogram(category: String) {
        val pictogram = documentPictogram(category)
        val fileName = WikiParser.pictogramFileName(pictogram.name)
        if (getAll().none { WikiParser.pictogramFileName(it.name) == fileName }) {
            insert(PictogramEntity(name = pictogram.name, icon = pictogram.icon))
        }
    }
}
