package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routine: RoutineEntity)

    @Query("SELECT * FROM routines ORDER BY name")
    fun getAllRoutines(): List<RoutineEntity>

    @Query("DELETE FROM routines WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM routines")
    fun deleteAll()

    @Update
    fun updateAll(map: List<RoutineEntity>)
}