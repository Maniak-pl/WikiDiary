package pl.maniak.wikidiary.feature.diary.data

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.diary.domain.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.diary.domain.Category

class CategoryRepositoryImpl(
    private val mapper: CategoryMapper,
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { categories -> categories.map(mapper::mapToDomain) }

    override suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        categoryDao.getAllCategories().map { mapper.mapToDomain(it) }
    }

    override suspend fun saveCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.insert(mapper.mapToEntity(category))
    }

    override suspend fun deleteCategory(id: Long) = withContext(Dispatchers.IO) {
        categoryDao.deleteById(id)
    }

    override suspend fun deleteAllCategories() = withContext(Dispatchers.IO) {
        categoryDao.deleteAll()
    }
}