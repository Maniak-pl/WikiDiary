package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.domain.model.WikiNote

interface WikiNoteRepository {

    suspend fun getNotes(): List<WikiNote>
    suspend fun getWikiNotesWithTag(tag: String): List<WikiNote>
    suspend fun saveNote(note: WikiNote)
    suspend fun saveNoteWithPeople(note: WikiNote, personIds: List<Long>): Long
    suspend fun deleteNoteById(id: Long)
    suspend fun deleteAllNotes()
}