package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.Category
import pl.maniak.wikidiary.data.database.CategoryDao
import pl.maniak.wikidiary.data.mapper.CategoryMapper

class CategoryRepositoryImpl(
    private val mapper: CategoryMapper,
    private val categoryDao: CategoryDao
) : CategoryRepository {
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