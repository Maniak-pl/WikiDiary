package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import pl.maniak.wikidiary.domain.model.FilmMediaType
import pl.maniak.wikidiary.domain.model.FilmSearchResult
import pl.maniak.wikidiary.utils.helpers.FilmwebPageParser
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder

class FilmwebSearchRepositoryImpl : FilmwebSearchRepository {

    override suspend fun search(query: String): List<FilmSearchResult> = withContext(Dispatchers.IO) {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotBlank()) { "Wpisz tytuł filmu lub serialu." }

        val searchResponse = requestJson(
            "/live/search?query=${URLEncoder.encode(normalizedQuery, Charsets.UTF_8.name())}"
        )
        val searchHits = searchResponse.optJSONArray("searchHits") ?: return@withContext emptyList()
        val results = mutableListOf<FilmSearchResult>()

        for (index in 0 until minOf(searchHits.length(), MAX_RESULTS)) {
            val hit = searchHits.optJSONObject(index) ?: continue
            val id = hit.optInt("id", -1)
            val mediaType = parseMediaType(hit.optString("type"))
            if (id <= 0 || mediaType == null) {
                continue
            }

            val details = try {
                requestJson("/title/$id/info")
            } catch (exception: FilmwebHttpException) {
                if (exception.statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    null
                } else {
                    throw exception
                }
            }

            results += toSearchResult(hit, details, id, mediaType)
        }

        results
    }

    private fun requestJson(path: String): JSONObject {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL("$API_BASE_URL$path").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = REQUEST_TIMEOUT_MS
                readTimeout = REQUEST_TIMEOUT_MS
                instanceFollowRedirects = true
                doInput = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseBody = responseStream
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (responseCode !in 200..299) {
                throw FilmwebHttpException(responseCode)
            }

            return try {
                JSONObject(responseBody)
            } catch (exception: JSONException) {
                throw FilmwebSearchException("Filmweb zwrócił nieprawidłową odpowiedź.", exception)
            }
        } finally {
            connection?.disconnect()
        }
    }

    private fun toSearchResult(
        hit: JSONObject,
        details: JSONObject?,
        id: Int,
        mediaType: FilmMediaType
    ): FilmSearchResult {
        val title = details
            ?.optString("title")
            ?.takeIf(String::isNotBlank)
            ?: hit.optString("matchedTitle").ifBlank { "Filmweb #$id" }
        val year = details
            ?.optInt("year", 0)
            ?.takeIf { it > 0 }
        val posterUrl = toPosterUrl(details?.optString("posterPath"))

        return FilmSearchResult(
            id = id,
            title = title,
            year = year,
            mediaType = mediaType,
            posterUrl = posterUrl,
            canonicalUrl = FilmwebPageParser.buildCanonicalUrl(mediaType, title, year, id)
        )
    }

    private fun parseMediaType(value: String): FilmMediaType? {
        return when (value.lowercase()) {
            "film" -> FilmMediaType.FILM
            "serial" -> FilmMediaType.SERIAL
            else -> null
        }
    }

    private fun toPosterUrl(posterPath: String?): String? {
        if (posterPath.isNullOrBlank()) {
            return null
        }

        val posterUri = runCatching {
            if (posterPath.startsWith("https://", ignoreCase = true)) {
                URI(posterPath)
            } else {
                URI("https://fwcdn.pl/fpo/${posterPath.trimStart('/')}")
            }
        }.getOrNull() ?: return null

        return posterUri
            .takeIf {
                it.scheme.equals("https", ignoreCase = true) &&
                    it.host.equals("fwcdn.pl", ignoreCase = true)
            }
            ?.toString()
    }

    private class FilmwebHttpException(
        val statusCode: Int
    ) : IOException("Filmweb zwrócił HTTP $statusCode.")

    class FilmwebSearchException(
        message: String,
        cause: Throwable? = null
    ) : IOException(message, cause)

    private companion object {
        const val API_BASE_URL = "https://www.filmweb.pl/api/v1"
        const val MAX_RESULTS = 10
        const val REQUEST_TIMEOUT_MS = 10_000
        const val USER_AGENT = "WikiDiary/1.0 (Filmweb search)"
    }
}
