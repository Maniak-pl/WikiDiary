package pl.maniak.wikidiary.feature.filmweb.data

import pl.maniak.wikidiary.feature.diary.data.WikiNoteEntity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "film_note_table",
    primaryKeys = ["noteId"],
    foreignKeys = [
        ForeignKey(
            entity = WikiNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FilmEntity::class,
            parentColumns = ["id"],
            childColumns = ["filmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["filmId"])]
)
data class FilmNoteEntity(
    val noteId: Long,
    val filmId: Long
)
