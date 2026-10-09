package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FilmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(film: FilmEntity): Long

    @Update
    fun update(film: FilmEntity)

    @Query("SELECT * FROM film_table WHERE canonicalUrl = :canonicalUrl LIMIT 1")
    fun getByCanonicalUrl(canonicalUrl: String): FilmEntity?
}
