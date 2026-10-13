package pl.maniak.wikidiary.feature.diary.data

import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.people.data.PersonMapper
import pl.maniak.wikidiary.feature.diary.domain.WikiNote

class WikiNoteRepositoryImpl(
    private val database: WikiNoteDatabase,
    private val wikiNoteDao: WikiNoteDao,
    private val mapper: WikiNoteMapper,
    private val notePersonDao: NotePersonDao,
    private val personMapper: PersonMapper
) : WikiNoteRepository {
    override fun observeNotes(): Flow<List<WikiNote>> =
        wikiNoteDao.observeAllWithPeople().map { rows ->
            rows.groupBy(NoteWithPersonRow::note, NoteWithPersonRow::person)
                .map { (entity, people) ->
                    mapper.mapToDomain(entity).copy(
                        people = people.filterNotNull().map(personMapper::mapToDomain)
                    )
                }
        }

    override suspend fun getNotes(): List<WikiNote> = withContext(Dispatchers.IO) {
        return@withContext mapNotes(wikiNoteDao.getAll())
    }

    override suspend fun getWikiNotesWithTag(tag: String): List<WikiNote> =
        withContext(Dispatchers.IO) {
            return@withContext mapNotes(wikiNoteDao.getAllByTag(tag))
        }

    override suspend fun saveNote(note: WikiNote) = withContext(Dispatchers.IO) {
        saveNoteWithPeople(note, note.people.map { it.id })
        Unit
    }

    override suspend fun saveNoteWithPeople(note: WikiNote, personIds: List<Long>): Long =
        withContext(Dispatchers.IO) {
            database.runInTransaction<Long> {
                val noteId = wikiNoteDao.insert(mapper.mapToEntity(note))
                notePersonDao.deleteByNoteId(noteId)
                val entries = personIds.distinct().mapIndexed { index, personId ->
                    NotePersonEntity(
                        noteId = noteId,
                        personId = personId,
                        position = index
                    )
                }
                if (entries.isNotEmpty()) {
                    notePersonDao.insertAll(entries)
                }
                noteId
            }
        }

    override suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) {
        wikiNoteDao.deleteById(id)
    }

    override suspend fun deleteAllNotes() = withContext(Dispatchers.IO) {
        wikiNoteDao.deleteAll()
    }

    private fun mapNotes(entities: List<WikiNoteEntity>): List<WikiNote> {
        return entities.map { entity ->
            mapper.mapToDomain(entity).copy(
                people = notePersonDao
                    .getPeopleForNote(entity.id)
                    .map(personMapper::mapToDomain)
            )
        }
    }
}