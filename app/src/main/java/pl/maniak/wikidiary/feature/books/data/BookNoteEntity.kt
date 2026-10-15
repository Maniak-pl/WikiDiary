package pl.maniak.wikidiary.feature.books.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pl.maniak.wikidiary.feature.diary.data.WikiNoteEntity

@Entity(
    tableName = "book_note_table",
    foreignKeys = [
        ForeignKey(
            entity = WikiNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookNoteEntity(
    @PrimaryKey val noteId: Long,
    val canonicalUrl: String,
    val title: String,
    val author: String,
    val year: Int?,
    val coverUrl: String?,
)
