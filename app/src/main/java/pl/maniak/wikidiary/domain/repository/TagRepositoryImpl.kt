package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.data.database.TagDao
import pl.maniak.wikidiary.data.mapper.TagMapper

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