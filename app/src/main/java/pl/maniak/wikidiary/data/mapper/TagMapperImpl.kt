package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.data.database.TagEntity

class TagMapperImpl: TagMapper {
    override fun mapToDomain(entity: TagEntity): Tag {
        return Tag(entity.id, entity.tag, entity.folder)
    }

    override fun mapToEntity(domain: Tag): TagEntity {
        return TagEntity(domain.id, domain.tag, domain.folder)
    }
}