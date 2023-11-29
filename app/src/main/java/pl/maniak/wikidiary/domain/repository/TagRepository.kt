package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.data.Tag

interface TagRepository {
    suspend fun getTags(): List<Tag>
    suspend fun saveTag(tag: Tag)
    suspend fun deleteTag(id: Long)
    suspend fun deleteAllTags()
}