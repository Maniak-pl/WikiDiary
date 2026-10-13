package pl.maniak.wikidiary.feature.diary.data

import pl.maniak.wikidiary.feature.diary.domain.WikiNote

interface WikiNoteMapper {
    fun mapToDomain(entity: WikiNoteEntity): WikiNote
    fun mapToEntity(domain: WikiNote): WikiNoteEntity
}