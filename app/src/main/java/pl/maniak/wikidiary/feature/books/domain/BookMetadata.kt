package pl.maniak.wikidiary.feature.books.domain

import java.net.URI
import java.net.URISyntaxException
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.diary.domain.WikiNote

data class BookMetadata(
    val canonicalUrl: String,
    val title: String,
    val author: String,
    val year: Int? = null,
    val coverUrl: String? = null,
) {
    fun isValid(): Boolean = isBookUrl(canonicalUrl) && title.isNotBlank() &&
        author.isNotBlank() && (year == null || year in 1000..9999) &&
        (coverUrl == null || isCoverUrl(coverUrl))

    companion object {
        fun isBookUrl(value: String): Boolean = uri(value)?.let {
            it.scheme == "https" && it.host in setOf("lubimyczytac.pl", "www.lubimyczytac.pl") &&
                it.port == -1 && it.userInfo == null &&
                Regex("/ksiazka/[1-9][0-9]*/[^/]+/?").matches(it.path.orEmpty()) &&
                it.query == null && it.fragment == null
        } == true

        fun isCoverUrl(value: String): Boolean = uri(value)?.let {
            it.scheme == "https" && it.host == "s.lubimyczytac.pl" &&
                it.port == -1 && it.userInfo == null && it.path.startsWith("/upload/books/")
        } == true

        private fun uri(value: String): URI? = try {
            URI(value)
        } catch (_: URISyntaxException) {
            null
        }
    }
}

data class BookNoteEntry(val noteId: Long, val metadata: BookMetadata)

interface BookRepository {
    fun observeBookNotes(): Flow<List<BookNoteEntry>>
    suspend fun saveBookNote(note: WikiNote, metadata: BookMetadata)
}

interface BookSearchRepository {
    suspend fun search(query: String): List<BookMetadata>
    suspend fun details(url: String): BookMetadata
}
