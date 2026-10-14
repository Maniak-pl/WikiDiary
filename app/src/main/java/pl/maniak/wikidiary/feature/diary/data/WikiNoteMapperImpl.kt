package pl.maniak.wikidiary.feature.diary.data

import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import java.util.Date

class WikiNoteMapperImpl : WikiNoteMapper {
    override fun mapToDomain(entity: WikiNoteEntity): WikiNote = WikiNote(
        id = entity.id,
        tag = entity.tag,
        content = entity.content,
        category = entity.folder,
        date = Date(entity.date),
        isSend = entity.isSend,
        needsCompletion = entity.needsCompletion
    )

    override fun mapToEntity(domain: WikiNote): WikiNoteEntity = WikiNoteEntity(
        id = domain.id,
        tag = domain.tag,
        content = domain.content,
        folder = domain.category,
        date = domain.date.time,
        isSend = domain.isSend,
        needsCompletion = domain.needsCompletion
    )
}