package pl.maniak.wikidiary.feature.books.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.books.domain.BookMetadata
import pl.maniak.wikidiary.feature.books.domain.BookSearchRepository

class LubimyczytacSearchRepository : BookSearchRepository {
    override suspend fun search(query: String): List<BookMetadata> = withContext(Dispatchers.IO) {
        require(query.isNotBlank()) { "Wpisz tytuł książki lub autora." }
        val url = "https://lubimyczytac.pl/szukaj/ksiazki?phrase=" +
            URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
        val html = request(url)
        ensureActive()
        LubimyczytacPageParser.search(html)
    }

    override suspend fun details(url: String): BookMetadata = withContext(Dispatchers.IO) {
        require(BookMetadata.isBookUrl(url)) { "Nieprawidłowy link do Lubimyczytać.pl." }
        val html = request(url)
        ensureActive()
        LubimyczytacPageParser.details(html, url)
    }

    private fun request(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "text/html")
            connection.setRequestProperty("User-Agent", "WikiDiary/1.1 (book search)")
            val status = connection.responseCode
            if (status !in 200..299) {
                throw IOException("Lubimyczytać.pl zwróciło HTTP $status. Spróbuj ponownie później.")
            }
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
