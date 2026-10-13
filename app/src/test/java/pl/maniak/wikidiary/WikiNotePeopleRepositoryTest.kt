package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapperImpl
import pl.maniak.wikidiary.feature.diary.data.WikiNoteRepositoryImpl
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.people.data.PersonMapper
import pl.maniak.wikidiary.feature.people.data.PersonRepositoryImpl
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import pl.maniak.wikidiary.feature.people.domain.PersonRepository
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class WikiNotePeopleRepositoryTest {

    private val database = inMemoryDatabase()
    private val people: PersonRepository = PersonRepositoryImpl(
        mapper = PersonMapper(),
        dao = database.personDao()
    )
    private val repository: WikiNoteRepository = WikiNoteRepositoryImpl(
        database = database,
        wikiNoteDao = database.wikiNoteDao(),
        mapper = WikiNoteMapperImpl(),
        notePersonDao = database.notePersonDao(),
        personMapper = PersonMapper()
    )
    private val note = WikiNote(
        tag = "Today",
        content = "Spotkanie z @Dorota Płachno",
        date = Date(1_728_480_000_000)
    )

    @After
    fun tearDown() = database.close()

    @Test
    fun savedNoteKeepsPeopleAfterPersonLeavesActiveDirectory() = runBlocking {
        val dorota = savedPerson("Dorota Płachno", PersonGender.FEMALE)

        repository.saveNoteWithPeople(note, listOf(dorota.id))
        people.deletePerson(dorota.id)

        assertEquals(listOf(dorota), repository.getNotes().single().people)
    }

    @Test
    fun observedNotesReflectSavedNoteWithPeople() = runBlocking {
        val dorota = savedPerson("Dorota Płachno", PersonGender.FEMALE)

        val notes = repository.observeNotes().nextValueAfter {
            repository.saveNoteWithPeople(note, listOf(dorota.id))
        }

        assertEquals("Spotkanie z @Dorota Płachno", notes.single().content)
        assertEquals(listOf(dorota), notes.single().people)
    }

    private suspend fun savedPerson(name: String, gender: PersonGender): Person {
        people.savePerson(Person(name = name, gender = gender))
        return people.getPeople().single { it.name == name }
    }
}
