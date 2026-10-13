package pl.maniak.wikidiary.feature.diary.domain

import kotlinx.coroutines.flow.Flow

interface WikiNoteRepository {
    fun observeNotes(): Flow<List<WikiNote>>

    suspend fun getNotes(): List<WikiNote>
    suspend fun getWikiNotesWithTag(tag: String): List<WikiNote>
    suspend fun saveNote(note: WikiNote)
    suspend fun saveNoteWithPeople(note: WikiNote, personIds: List<Long>): Long
    suspend fun deleteNoteById(id: Long)
    suspend fun deleteAllNotes()
}