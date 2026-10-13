package pl.maniak.wikidiary.feature.people.data

import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender

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
