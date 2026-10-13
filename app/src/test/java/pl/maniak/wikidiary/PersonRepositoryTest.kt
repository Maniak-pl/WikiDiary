package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import pl.maniak.wikidiary.feature.people.data.PersonDao
import pl.maniak.wikidiary.feature.people.data.PersonEntity
import pl.maniak.wikidiary.feature.people.data.PersonMapper
import pl.maniak.wikidiary.feature.people.domain.PersonRepository
import pl.maniak.wikidiary.feature.people.data.PersonRepositoryImpl

class PersonRepositoryTest {

    @Test
    fun peopleCanBeCreatedAndRemovedFromActiveDirectory() = runBlocking {
        val dao = InMemoryPersonDao()
        val repository: PersonRepository = PersonRepositoryImpl(
            mapper = PersonMapper(),
            dao = dao
        )

        repository.savePerson(Person(name = "Dorota Płachno", gender = PersonGender.FEMALE))
        repository.savePerson(Person(name = "Damian Hanusiak", gender = PersonGender.MALE))

        assertEquals(
            listOf("Damian Hanusiak", "Dorota Płachno"),
            repository.getPeople().map(Person::name)
        )
        assertEquals(
            PersonGender.FEMALE,
            repository.getPeople().first { it.name == "Dorota Płachno" }.gender
        )

        repository.deletePerson(dao.activeValues.first { it.name == "Dorota Płachno" }.id)

        assertTrue(repository.getPeople().none { it.name == "Dorota Płachno" })
    }
}

private class InMemoryPersonDao : PersonDao {
    val activeValues = mutableListOf<PersonEntity>()
    private val archivedValues = mutableListOf<PersonEntity>()
    private var nextId = 1L

    override fun getAll(): List<PersonEntity> = activeValues.sortedBy { it.name }

    override fun getById(id: Long): PersonEntity? {
        return (activeValues + archivedValues).firstOrNull { it.id == id }
    }

    override fun insert(person: PersonEntity): Long {
        val stored = person.copy(id = if (person.id == 0L) nextId++ else person.id)
        activeValues += stored
        return stored.id
    }

    override fun archiveById(id: Long) {
        val person = activeValues.first { it.id == id }
        activeValues.remove(person)
        archivedValues += person.copy(isActive = false)
    }
}
