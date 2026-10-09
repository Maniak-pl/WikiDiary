package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.database.FilmDao
import pl.maniak.wikidiary.data.database.FilmEntity
import pl.maniak.wikidiary.data.database.FilmNoteDao
import pl.maniak.wikidiary.data.database.FilmNoteEntity
import pl.maniak.wikidiary.data.database.WikiNoteDatabase
import pl.maniak.wikidiary.data.database.WikiNoteDao
import pl.maniak.wikidiary.data.mapper.WikiNoteMapper
import pl.maniak.wikidiary.domain.model.FilmMediaType
import pl.maniak.wikidiary.domain.model.FilmMetadata
import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.WikiNote

class FilmRepositoryImpl(
    private val database: WikiNoteDatabase,
    private val wikiNoteDao: WikiNoteDao,
    private val filmDao: FilmDao,
    private val filmNoteDao: FilmNoteDao,
    private val wikiNoteMapper: WikiNoteMapper
) : FilmRepository {

    override suspend fun saveFilmNote(note: WikiNote, metadata: FilmMetadata) {
        withContext(Dispatchers.IO) {
            database.runInTransaction {
                val noteId = wikiNoteDao.insert(wikiNoteMapper.mapToEntity(note))
                val film = FilmEntity(
                    canonicalUrl = metadata.canonicalUrl,
                    title = metadata.title,
                    year = metadata.year,
                    mediaType = metadata.mediaType.name,
                    posterUrl = metadata.posterUrl
                )
                val insertedFilmId = filmDao.insert(film)
                val storedFilm = filmDao.getByCanonicalUrl(metadata.canonicalUrl)
                    ?: error("Film metadata could not be stored")
                val filmId = storedFilm.id
                if (insertedFilmId == -1L) {
                    filmDao.update(film.copy(id = filmId))
                }
                filmNoteDao.insert(FilmNoteEntity(noteId = noteId, filmId = filmId))
            }
        }
    }

    override suspend fun getFilmNotes(): List<FilmNoteEntry> = withContext(Dispatchers.IO) {
        filmNoteDao.getAll().map { record ->
            FilmNoteEntry(
                noteId = record.noteId,
                metadata = FilmMetadata(
                    canonicalUrl = record.canonicalUrl,
                    title = record.title,
                    year = record.year,
                    mediaType = FilmMediaType.valueOf(record.mediaType),
                    posterUrl = record.posterUrl
                )
            )
        }
    }
}
