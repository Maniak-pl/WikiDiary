package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.data.database.TagEntity

interface TagMapper {
    fun mapToDomain(entity: TagEntity): Tag

    fun mapToEntity(domain: Tag): TagEntity
}