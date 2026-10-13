package pl.maniak.wikidiary.feature.diary.data

import pl.maniak.wikidiary.feature.diary.domain.Tag

interface TagMapper {
    fun mapToDomain(entity: TagEntity): Tag

    fun mapToEntity(domain: Tag): TagEntity
}