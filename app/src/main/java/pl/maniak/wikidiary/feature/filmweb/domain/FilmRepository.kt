package pl.maniak.wikidiary.feature.filmweb.domain

import pl.maniak.wikidiary.feature.diary.domain.WikiNote

interface FilmRepository {
    suspend fun saveFilmNote(note: WikiNote, metadata: FilmMetadata)
    suspend fun getFilmNotes(): List<FilmNoteEntry>
}
