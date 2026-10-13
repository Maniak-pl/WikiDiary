package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import pl.maniak.wikidiary.data.Person
import pl.maniak.wikidiary.data.PersonGender
import pl.maniak.wikidiary.data.database.NotePersonDao
import pl.maniak.wikidiary.data.database.NotePersonEntity
import pl.maniak.wikidiary.data.database.PersonEntity
import pl.maniak.wikidiary.data.database.WikiNoteDao
import pl.maniak.wikidiary.data.database.WikiNoteEntity
import pl.maniak.wikidiary.data.mapper.PersonMapper
import pl.maniak.wikidiary.data.mapper.WikiNoteMapperImpl
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.domain.repository.WikiNoteRepository
import pl.maniak.wikidiary.domain.repository.WikiNoteRepositoryImpl
import java.util.Date

class WikiNotePeopleRepositoryTest {

    @Test
    fun savedNoteKeepsPeopleAfterPersonLeavesActiveDirectory() = runBlocking {
        val people = mutableMapOf(
            7L to PersonEntity(
                id = 7,
                name = "Dorota Płachno",
                gender = PersonGender.FEMALE.storageValue
            )
        )
        val noteDao = InMemoryWikiNoteDao()
        val notePersonDao = InMemoryNotePersonDao(people)
        val repository: WikiNoteRepository = WikiNoteRepositoryImpl(
            wikiNoteDao = noteDao,
            mapper = WikiNoteMapperImpl(),
            notePersonDao = notePersonDao,
            personMapper = PersonMapper()
        )
        val note = WikiNote(
            tag = "Today",
            content = "Spotkanie z @Dorota Płachno",
            date = Date(1_728_480_000_000)
        )

        repository.saveNoteWithPeople(note, listOf(7L))
        people[7L] = people.getValue(7L).copy(isActive = false)

        assertEquals(
            listOf(Person(7, "Dorota Płachno", PersonGender.FEMALE)),
            repository.getNotes().single().people
        )
    }
}

private class InMemoryWikiNoteDao : WikiNoteDao {
    private val values = linkedMapOf<Long, WikiNoteEntity>()
    private var nextId = 1L

    override fun getAll(): List<WikiNoteEntity> = values.values.toList()

    override fun getAllByTag(tag: String): List<WikiNoteEntity> {
        return values.values.filter { it.tag == tag }
    }

    override fun insert(wikiNote: WikiNoteEntity): Long {
        val id = if (wikiNote.id == 0L) nextId++ else wikiNote.id
        values[id] = wikiNote.copy(id = id)
        return id
    }

    override fun deleteById(id: Long) {
        values.remove(id)
    }

    override fun deleteAll() {
        values.clear()
    }
}

private class InMemoryNotePersonDao(
    private val people: Map<Long, PersonEntity>
) : NotePersonDao {
    private val values = mutableMapOf<Long, List<NotePersonEntity>>()

    override fun getPeopleForNote(noteId: Long): List<PersonEntity> {
        return values[noteId]
            .orEmpty()
            .sortedBy(NotePersonEntity::position)
            .mapNotNull { people[it.personId] }
    }

    override fun insertAll(entries: List<NotePersonEntity>) {
        if (entries.isNotEmpty()) {
            values[entries.first().noteId] = entries
        }
    }

    override fun deleteByNoteId(noteId: Long) {
        values.remove(noteId)
    }
}
