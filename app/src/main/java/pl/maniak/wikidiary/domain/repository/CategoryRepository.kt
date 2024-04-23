package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.data.Category

interface CategoryRepository {
    suspend fun getCategories(): List<Category>
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    suspend fun deleteAllCategories()
}