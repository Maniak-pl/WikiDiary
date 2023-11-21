package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.database.WikiNoteEntity
import pl.maniak.wikidiary.domain.model.WikiNote

interface WikiNoteMapper {
    fun mapToDomain(entity: WikiNoteEntity): WikiNote
    fun mapToEntity(domain: WikiNote): WikiNoteEntity
}