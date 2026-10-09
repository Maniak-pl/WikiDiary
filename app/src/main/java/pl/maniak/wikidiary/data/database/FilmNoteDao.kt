package pl.maniak.wikidiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FilmNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(filmNote: FilmNoteEntity)

    @Query(
        """
        SELECT
            link.noteId AS noteId,
            film.canonicalUrl AS canonicalUrl,
            film.title AS title,
            film.year AS year,
            film.mediaType AS mediaType,
            film.posterUrl AS posterUrl
        FROM film_note_table AS link
        INNER JOIN film_table AS film ON film.id = link.filmId
        """
    )
    fun getAll(): List<FilmNoteRecord>
}
