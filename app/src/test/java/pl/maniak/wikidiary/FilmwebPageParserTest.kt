package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.domain.model.FilmMediaType
import pl.maniak.wikidiary.utils.helpers.FilmwebPageParser

class FilmwebPageParserTest {
    @Test
    fun canonicalizeUrl_acceptsFilmwebFilmAndRemovesTrackingParts() {
        val result = FilmwebPageParser.canonicalizeUrl(
            "https://www.filmweb.pl/film/example-2020?utm_source=test#cast"
        )

        assertEquals("https://www.filmweb.pl/film/example-2020", result)
    }

    @Test
    fun canonicalizeUrl_rejectsUnsupportedHostsAndPaths() {
        assertNull(FilmwebPageParser.canonicalizeUrl("http://www.filmweb.pl/film/example-2020"))
        assertNull(FilmwebPageParser.canonicalizeUrl("https://filmweb.pl/film/example-2020"))
        assertNull(FilmwebPageParser.canonicalizeUrl("https://www.filmweb.pl/video/example"))
        assertNull(FilmwebPageParser.canonicalizeUrl("https://www.filmweb.pl/film"))
    }

    @Test
    fun parse_extractsEditableMetadata() {
        val result = FilmwebPageParser.parse(
            rawUrl = "https://www.filmweb.pl/serial/example-2021/",
            rawTitle = "Przykładowy serial (2021) - Filmweb",
            posterUrl = "https://fwcdn.pl/fpo/example.jpg"
        )

        requireNotNull(result)
        assertEquals("https://www.filmweb.pl/serial/example-2021", result.canonicalUrl)
        assertEquals("Przykładowy serial", result.title)
        assertEquals(2021, result.year)
        assertEquals(FilmMediaType.SERIAL, result.mediaType)
        assertEquals("https://fwcdn.pl/fpo/example.jpg", result.posterUrl)
    }

    @Test
    fun sanitizeImageFileName_removesPolishCharactersAndUnsafeCharacters() {
        val result = FilmwebPageParser.sanitizeImageFileName("Żółć Łódź: część 2!", 2024)

        assertEquals("Zolc_Lodz_czesc_2_2024.jpg", result)
        assertTrue(result.all { it.isLetterOrDigit() || it == '_' || it == '.' })
    }

    @Test
    fun buildCanonicalUrl_createsFilmwebTitleUrl() {
        val result = FilmwebPageParser.buildCanonicalUrl(
            mediaType = FilmMediaType.FILM,
            title = "Żółć: część 2!",
            year = 2024,
            id = 12345
        )

        assertEquals(
            "https://www.filmweb.pl/film/Zolc-czesc-2-2024-12345",
            result
        )
    }
}
