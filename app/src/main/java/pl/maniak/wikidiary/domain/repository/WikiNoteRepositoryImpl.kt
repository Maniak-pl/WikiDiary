package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.database.NotePersonDao
import pl.maniak.wikidiary.data.database.NotePersonEntity
import pl.maniak.wikidiary.data.database.WikiNoteDao
import pl.maniak.wikidiary.data.database.WikiNoteEntity
import pl.maniak.wikidiary.data.mapper.WikiNoteMapper
import pl.maniak.wikidiary.data.mapper.PersonMapper
import pl.maniak.wikidiary.domain.model.WikiNote

class WikiNoteRepositoryImpl(
    private val wikiNoteDao: WikiNoteDao,
    private val mapper: WikiNoteMapper,
    private val notePersonDao: NotePersonDao,
    private val personMapper: PersonMapper
) : WikiNoteRepository {

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