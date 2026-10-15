package pl.maniak.wikidiary.feature.books.data

import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.books.domain.BookMetadata
import pl.maniak.wikidiary.feature.books.domain.BookNoteEntry
import pl.maniak.wikidiary.feature.books.domain.BookRepository
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapper
import pl.maniak.wikidiary.feature.diary.domain.WikiNote

class BookRepositoryImpl(
    private val database: WikiNoteDatabase,
    private val noteMapper: WikiNoteMapper,
) : BookRepository {
    override fun observeBookNotes() = database.bookNoteDao().observeAll().map { books ->
        books.map {
            BookNoteEntry(it.noteId, BookMetadata(it.canonicalUrl, it.title, it.author, it.year, it.coverUrl))
        }
    }

    override suspend fun saveBookNote(note: WikiNote, metadata: BookMetadata) = withContext(Dispatchers.IO) {
        require(metadata.isValid()) { "Uzupełnij poprawne dane książki." }
        database.runInTransaction {
            val noteId = database.wikiNoteDao().insert(noteMapper.mapToEntity(note))
            database.bookNoteDao().insert(
                BookNoteEntity(
                    noteId, metadata.canonicalUrl, metadata.title, metadata.author, metadata.year, metadata.coverUrl
                )
            )
            database.tagDao().getAll().firstOrNull { it.tag == note.tag }?.let {
                database.tagDao().insert(it.copy(date = Date()))
            }
        }
    }
}
