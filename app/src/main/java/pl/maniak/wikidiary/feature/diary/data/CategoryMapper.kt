package pl.maniak.wikidiary.feature.diary.data

import pl.maniak.wikidiary.feature.diary.domain.Category

class CategoryMapper {
    fun mapToDomain(entity: CategoryEntity): Category {
        return Category(entity.id, entity.name)
    }

    fun mapToEntity(domain: Category): CategoryEntity {
        return CategoryEntity(domain.id, domain.name)
    }
}