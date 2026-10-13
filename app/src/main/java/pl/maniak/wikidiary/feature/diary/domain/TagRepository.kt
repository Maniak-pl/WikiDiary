package pl.maniak.wikidiary.feature.diary.domain

import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeTags(): Flow<List<Tag>>
    suspend fun getTags(): List<Tag>
    suspend fun saveTag(tag: Tag)
    suspend fun deleteTag(id: Long)
    suspend fun deleteAllTags()
}