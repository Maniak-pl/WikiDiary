package pl.maniak.wikidiary.data.database

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
