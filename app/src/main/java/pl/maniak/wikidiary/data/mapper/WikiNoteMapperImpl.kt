package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.database.WikiNoteEntity
import pl.maniak.wikidiary.domain.model.WikiNote
import java.util.Date

class WikiNoteMapperImpl : WikiNoteMapper {
    override fun mapToDomain(entity: WikiNoteEntity): WikiNote {
        return WikiNote(
            id = entity.id,
            tag = entity.tag,
            content = entity.content,
            date = Date(entity.date),
            isSend = entity.isSend
        )
    }

    override fun mapToEntity(domain: WikiNote): WikiNoteEntity {
        return WikiNoteEntity(
            id = domain.id,
            tag = domain.tag,
            content = domain.content,
            date = domain.date.time,
            isSend = domain.isSend
        )
    }
}