package pl.maniak.wikidiary.feature.documents

import android.content.SharedPreferences
import androidx.room.withTransaction
import kotlinx.serialization.json.Json
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.diary.data.WikiNoteEntity

class DocumentStore(
    private val preferences: SharedPreferences,
    private val database: WikiNoteDatabase,
) {
    fun pending(): DocumentDraft? = preferences.getString("pending_document", null)?.let {
        Json { ignoreUnknownKeys = true }.decodeFromString<DocumentDraft>(it)
    }

    fun setPending(draft: DocumentDraft?) {
        val editor = preferences.edit()
        if (draft == null) editor.remove("pending_document")
        else editor.putString("pending_document", Json.encodeToString(DocumentDraft.serializer(), draft))
        check(editor.commit()) { "Nie udało się zapisać stanu przesyłania." }
    }

    suspend fun saveNote(draft: DocumentDraft) {
        val note = draft.note()
        database.withTransaction {
            draft.documentCategory?.takeIf { it.isNotBlank() }?.let {
                database.pictogramDao().ensureDocumentPictogram(it)
            }
            val dao = database.wikiNoteDao()
            if (!dao.hasDocumentNote(note.tag, note.date.time, note.content)) {
                dao.insert(WikiNoteEntity(
                    tag = note.tag,
                    content = note.content,
                    folder = note.category,
                    date = note.date.time,
                    isSend = false,
                ))
            }
        }
    }
}
