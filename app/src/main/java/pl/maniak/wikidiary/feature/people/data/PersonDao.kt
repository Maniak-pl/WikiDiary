package pl.maniak.wikidiary.feature.people.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PersonDao {
    @Query("SELECT * FROM person_table WHERE isActive = 1 ORDER BY name COLLATE NOCASE")
    fun getAll(): List<PersonEntity>

    @Query("SELECT * FROM person_table WHERE isActive = 1 ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM person_table WHERE id = :id")
    fun getById(id: Long): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(person: PersonEntity): Long

    @Query("UPDATE person_table SET isActive = 0 WHERE id = :id")
    fun archiveById(id: Long)
}
