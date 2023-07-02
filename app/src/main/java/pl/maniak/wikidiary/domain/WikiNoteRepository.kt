package pl.maniak.wikidiary.domain

import pl.maniak.wikidiary.data.WikiNote

interface WikiNoteRepository {

    fun getNotes(): List<WikiNote>
    fun getWikiNotesWithTag(tag: String): List<WikiNote>
    fun saveNote(note : WikiNote)
    fun deleteNoteById(id:Long)
    fun deleteAllNotes()
}