package pl.maniak.wikidiary.utils.helpers

import pl.maniak.wikidiary.domain.model.FilmMediaType
import java.net.URI
import java.text.Normalizer

data class FilmwebPageMetadata(
    val canonicalUrl: String,
    val title: String,
    val year: Int?,
    val mediaType: FilmMediaType,
    val posterUrl: String?
)

object FilmwebPageParser {
    private const val filmwebHost = "www.filmweb.pl"
    private val yearPattern = Regex("""\((\d{4})\)\s*$""")
    private val filmwebSuffixPattern = Regex("""\s+[-|]\s+Filmweb\s*$""")

    fun canonicalizeUrl(rawUrl: String): String? {
        val uri = runCatching { URI(rawUrl.trim()) }.getOrNull() ?: return null
        val path = uri.path ?: return null
        val pathSegments = path.split('/').filter(String::isNotBlank)
        val mediaSegment = pathSegments.firstOrNull()?.lowercase()
        if (
            !uri.scheme.equals("https", ignoreCase = true) ||
            !uri.host.equals(filmwebHost, ignoreCase = true) ||
            uri.port != -1 ||
            uri.userInfo != null ||
            pathSegments.size < 2 ||
            mediaSegment !in setOf("film", "serial")
        ) {
            return null
        }

        val canonicalPath = uri.rawPath?.trimEnd('/').orEmpty()
        return "https://$filmwebHost$canonicalPath"
    }

    fun parse(
        rawUrl: String,
        rawTitle: String?,
        posterUrl: String?
    ): FilmwebPageMetadata? {
        val canonicalUrl = canonicalizeUrl(rawUrl) ?: return null
        val mediaType = when {
            canonicalUrl.substringAfter("https://$filmwebHost/")
                .substringBefore('/')
                .equals("film", ignoreCase = true) -> FilmMediaType.FILM
            canonicalUrl.substringAfter("https://$filmwebHost/")
                .substringBefore('/')
                .equals("serial", ignoreCase = true) -> FilmMediaType.SERIAL
            else -> return null
        }
        val titleWithYear = rawTitle
            ?.replace(filmwebSuffixPattern, "")
            ?.trim()
            .orEmpty()
        val year = yearPattern.find(titleWithYear)?.groupValues?.get(1)?.toIntOrNull()
        val title = titleWithYear.replace(yearPattern, "").trim()

        return FilmwebPageMetadata(
            canonicalUrl = canonicalUrl,
            title = title,
            year = year,
            mediaType = mediaType,
            posterUrl = posterUrl?.takeIf(::isHttpsUrl)
        )
    }

    fun sanitizeImageFileName(title: String, year: Int): String {
        val asciiTitle = toAscii(title)
        val normalizedTitle = asciiTitle
            .replace("[^A-Za-z0-9]+".toRegex(), "_")
            .trim('_')
            .ifBlank { "Film" }
        return "${normalizedTitle}_$year.jpg"
    }

    fun buildCanonicalUrl(
        mediaType: FilmMediaType,
        title: String,
        year: Int?,
        id: Int
    ): String {
        val mediaSegment = when (mediaType) {
            FilmMediaType.FILM -> "film"
            FilmMediaType.SERIAL -> "serial"
        }
        val slug = toAscii(title)
            .replace("[^A-Za-z0-9]+".toRegex(), "-")
            .trim('-')
            .ifBlank { "title" }
        val yearSuffix = year?.let { "-$it" }.orEmpty()
        return "https://$filmwebHost/$mediaSegment/$slug$yearSuffix-$id"
    }

    private fun toAscii(value: String): String {
        return Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .replace('ą', 'a')
            .replace('Ą', 'A')
            .replace('ć', 'c')
            .replace('Ć', 'C')
            .replace('ę', 'e')
            .replace('Ę', 'E')
            .replace('ł', 'l')
            .replace('Ł', 'L')
            .replace('ń', 'n')
            .replace('Ń', 'N')
            .replace('ó', 'o')
            .replace('Ó', 'O')
            .replace('ś', 's')
            .replace('Ś', 'S')
            .replace('ź', 'z')
            .replace('Ź', 'Z')
            .replace('ż', 'z')
            .replace('Ż', 'Z')
    }

    private fun isHttpsUrl(url: String): Boolean {
        return runCatching {
            val uri = URI(url)
            uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }
}
