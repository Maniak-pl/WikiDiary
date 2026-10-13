package pl.maniak.wikidiary.feature.filmweb.data

import pl.maniak.wikidiary.feature.filmweb.domain.FilmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.diary.data.WikiNoteDao
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapper
import pl.maniak.wikidiary.feature.filmweb.domain.FilmMediaType
import pl.maniak.wikidiary.feature.filmweb.domain.FilmMetadata
import pl.maniak.wikidiary.feature.filmweb.domain.FilmNoteEntry
import pl.maniak.wikidiary.feature.diary.domain.WikiNote

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
