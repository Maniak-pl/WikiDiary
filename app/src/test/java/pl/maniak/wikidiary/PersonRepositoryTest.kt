package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.people.data.PersonMapper
import pl.maniak.wikidiary.feature.people.data.PersonRepositoryImpl
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import pl.maniak.wikidiary.feature.people.domain.PersonRepository
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class PersonRepositoryTest {

    private val database = inMemoryDatabase()
    private val repository: PersonRepository = PersonRepositoryImpl(
        mapper = PersonMapper(),
        dao = database.personDao()
    )

    @After
    fun tearDown() = database.close()

    @Test
    fun peopleCanBeCreatedAndRemovedFromActiveDirectory() = runBlocking {
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

        repository.deletePerson(repository.getPeople().first { it.name == "Dorota Płachno" }.id)

        assertTrue(repository.getPeople().none { it.name == "Dorota Płachno" })
    }

    @Test
    fun observedPeopleReflectSavedPerson() = runBlocking {
        val people = repository.observePeople().nextValueAfter {
            repository.savePerson(Person(name = "Jakub Hałun", gender = PersonGender.MALE))
        }

        assertEquals(listOf("Jakub Hałun"), people.map(Person::name))
    }
}
