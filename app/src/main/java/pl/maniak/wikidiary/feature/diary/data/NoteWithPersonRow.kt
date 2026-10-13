package pl.maniak.wikidiary.feature.diary.data

import androidx.room.Embedded
import pl.maniak.wikidiary.feature.people.data.PersonEntity

data class NoteWithPersonRow(
    @Embedded val note: WikiNoteEntity,
    @Embedded(prefix = "person_") val person: PersonEntity?
)
