package pl.maniak.wikidiary.feature.diary.data

import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.diary.domain.Tag

class TagRepositoryImpl(
    private val mapper: TagMapper,
    private val tagDao: TagDao
) : TagRepository {
    override suspend fun getTags(): List<Tag> = withContext(Dispatchers.IO) {
        tagDao.getAll().map { mapper.mapToDomain(it) }
    }

    override suspend fun saveTag(tag: Tag) = withContext(Dispatchers.IO) {
        tagDao.insert(mapper.mapToEntity(tag))
    }

    override suspend fun deleteTag(id: Long) = withContext(Dispatchers.IO) {
        tagDao.deleteById(id)
    }

    override suspend fun deleteAllTags() = withContext(Dispatchers.IO) {
        tagDao.deleteAll()
    }
}