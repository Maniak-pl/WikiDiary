package pl.maniak.wikidiary.domain

import pl.maniak.wikidiary.data.WikiNote

class WikiNoteRepositoryImpl : WikiNoteRepository {

    private var list = mutableListOf<WikiNote>()

    override fun getNotes(): List<WikiNote> {

        return list
    }

    override fun getWikiNotesWithTag(tag: String): List<WikiNote> {
        return list.filter { it.tag == tag }
    }

    override fun saveNote(note: WikiNote) {
        list.add(note)
    }

    override fun deleteNoteById(id: Long) {
        list.removeAll { item -> item.id == id }
    }

    override fun deleteAllNotes() {
        list.clear()
    }
}