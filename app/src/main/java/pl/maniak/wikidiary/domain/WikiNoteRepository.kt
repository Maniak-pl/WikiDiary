package pl.maniak.wikidiary.domain

import pl.maniak.wikidiary.data.WikiNote

interface WikiNoteRepository {

    fun getNotes(): MutableList<WikiNote>
    fun getWikiNotesWithTag(tag: String): MutableList<WikiNote>
    fun saveNote(note : WikiNote)
    fun deleteNoteById(id:Long)
    fun deleteAllNotes()
}