package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query

@Dao
interface CategoryDao {
    @Insert(onConflict = REPLACE)
    fun insert(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): List<CategoryEntity>

    @Query("DELETE FROM categories WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM categories")
    fun deleteAll()
}