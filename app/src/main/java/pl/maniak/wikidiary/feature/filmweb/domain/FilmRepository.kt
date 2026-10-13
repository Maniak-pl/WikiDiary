package pl.maniak.wikidiary.feature.filmweb.domain

import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.diary.domain.WikiNote

interface FilmRepository {
    fun observeFilmNotes(): Flow<List<FilmNoteEntry>>
    suspend fun saveFilmNote(note: WikiNote, metadata: FilmMetadata)
    suspend fun getFilmNotes(): List<FilmNoteEntry>
}
