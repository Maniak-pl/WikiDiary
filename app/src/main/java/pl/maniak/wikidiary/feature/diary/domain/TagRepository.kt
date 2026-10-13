package pl.maniak.wikidiary.feature.diary.domain


interface TagRepository {
    suspend fun getTags(): List<Tag>
    suspend fun saveTag(tag: Tag)
    suspend fun deleteTag(id: Long)
    suspend fun deleteAllTags()
}