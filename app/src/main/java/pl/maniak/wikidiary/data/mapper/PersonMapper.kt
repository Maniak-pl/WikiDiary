package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.Person
import pl.maniak.wikidiary.data.PersonGender
import pl.maniak.wikidiary.data.database.PersonEntity

class PersonMapper {
    fun mapToDomain(entity: PersonEntity): Person {
        return Person(
            id = entity.id,
            name = entity.name,
            gender = PersonGender.fromStorage(entity.gender)
        )
    }

    fun mapToEntity(domain: Person): PersonEntity {
        return PersonEntity(
            id = domain.id,
            name = domain.name,
            gender = domain.gender.storageValue
        )
    }
}
