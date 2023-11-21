package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.database.WikiNoteDao
import pl.maniak.wikidiary.data.mapper.WikiNoteMapper
import pl.maniak.wikidiary.domain.model.WikiNote

class WikiNoteRepositoryImpl(
    private val wikiNoteDao: WikiNoteDao,
    private val mapper: WikiNoteMapper
) : WikiNoteRepository {

    override suspend fun getNotes(): List<WikiNote> = withContext(Dispatchers.IO) {
        val entities = wikiNoteDao.getAll()
        return@withContext entities.map { mapper.mapToDomain(it) }

    }

    override suspend fun getWikiNotesWithTag(tag: String): List<WikiNote> =
        withContext(Dispatchers.IO) {
            val entities = wikiNoteDao.getAllByTag(tag)
            return@withContext entities.map { mapper.mapToDomain(it) }
        }

    override suspend fun saveNote(note: WikiNote) = withContext(Dispatchers.IO) {
        val entity = mapper.mapToEntity(note)
        wikiNoteDao.insert(entity)
    }

    override suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) {
        wikiNoteDao.deleteById(id)
    }

    override suspend fun deleteAllNotes() = withContext(Dispatchers.IO) {
        wikiNoteDao.deleteAll()
    }
}