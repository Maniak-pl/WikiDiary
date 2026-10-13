package pl.maniak.wikidiary.feature.people.data

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.people.domain.PersonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.people.domain.Person

class PersonRepositoryImpl(
    private val mapper: PersonMapper,
    private val dao: PersonDao
) : PersonRepository {
    override fun observePeople(): Flow<List<Person>> =
        dao.observeAll().map { people -> people.map(mapper::mapToDomain) }

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
