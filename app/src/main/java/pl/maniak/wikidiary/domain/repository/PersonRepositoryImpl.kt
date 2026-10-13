package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.Person
import pl.maniak.wikidiary.data.database.PersonDao
import pl.maniak.wikidiary.data.mapper.PersonMapper

class PersonRepositoryImpl(
    private val mapper: PersonMapper,
    private val dao: PersonDao
) : PersonRepository {
    override suspend fun getPeople(): List<Person> = withContext(Dispatchers.IO) {
        dao.getAll().map(mapper::mapToDomain)
    }

    override suspend fun savePerson(person: Person) = withContext(Dispatchers.IO) {
        dao.insert(mapper.mapToEntity(person))
        Unit
    }

    override suspend fun deletePerson(id: Long) = withContext(Dispatchers.IO) {
        dao.archiveById(id)
    }
}
