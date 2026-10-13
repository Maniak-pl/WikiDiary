package pl.maniak.wikidiary.feature.diary.domain


interface CategoryRepository {
    suspend fun getCategories(): List<Category>
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    suspend fun deleteAllCategories()
}