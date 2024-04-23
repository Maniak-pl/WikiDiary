package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.Category
import pl.maniak.wikidiary.data.database.CategoryEntity

class CategoryMapper {
    fun mapToDomain(entity: CategoryEntity): Category {
        return Category(entity.id, entity.name)
    }

    fun mapToEntity(domain: Category): CategoryEntity {
        return CategoryEntity(domain.id, domain.name)
    }
}