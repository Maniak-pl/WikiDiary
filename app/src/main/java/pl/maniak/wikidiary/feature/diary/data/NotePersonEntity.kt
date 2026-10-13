package pl.maniak.wikidiary.feature.diary.data

import pl.maniak.wikidiary.feature.people.data.PersonEntity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "note_person_table",
    primaryKeys = ["noteId", "personId"],
    foreignKeys = [
        ForeignKey(
            entity = WikiNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["noteId"]),
        Index(value = ["personId"])
    ]
)
data class NotePersonEntity(
    val noteId: Long,
    val personId: Long,
    val position: Int
)
