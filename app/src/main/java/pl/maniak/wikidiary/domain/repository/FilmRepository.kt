package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.domain.model.FilmMetadata
import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.WikiNote

interface FilmRepository {
    suspend fun saveFilmNote(note: WikiNote, metadata: FilmMetadata)
    suspend fun getFilmNotes(): List<FilmNoteEntry>
}
