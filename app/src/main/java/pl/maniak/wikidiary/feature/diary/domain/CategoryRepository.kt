package pl.maniak.wikidiary.feature.diary.domain

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getCategories(): List<Category>
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    suspend fun deleteAllCategories()
}