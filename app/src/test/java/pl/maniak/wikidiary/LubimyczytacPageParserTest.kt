package pl.maniak.wikidiary

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.feature.books.data.LubimyczytacPageParser
import pl.maniak.wikidiary.feature.books.domain.BookMetadata

class LubimyczytacPageParserTest {
    private val url = "https://lubimyczytac.pl/ksiazka/123/tytul"
    private val cover = "https://s.lubimyczytac.pl/upload/books/0/123/cover.jpg"

    @Test
    fun searchExtractsOnlyActualResultsNotPromotedBooksAndDecodesEntities() {
        val card = """
            <div class="book-card">
              <a class="book-card__title" href="/ksiazka/123/tytul">Tytuł &amp; świat</a>
              <div class="book-card__author"><a>Anna Kowalska</a>, <a>Jan Nowak</a></div>
              <img class="book-card__cover-image" src="$cover">
            </div>
        """.trimIndent()
        val html = "<aside>$card</aside><main><div class='listSearch'>$card$card</div></main>"
        val result = LubimyczytacPageParser.search(html).single()
        assertEquals(url, result.canonicalUrl)
        assertEquals("Tytuł & świat", result.title)
        assertEquals("Anna Kowalska, Jan Nowak", result.author)
        assertEquals(cover, result.coverUrl)
    }

    @Test
    fun searchLimitsResultsToTenAndAllowsMissingCover() {
        val cards = (1..12).joinToString("") {
            """<div class="book-card"><a class="book-card__title" href="/ksiazka/$it/tytul">Tytuł $it</a>
                <div class="book-card__author"><a>Autor</a></div></div>"""
        }
        val result = LubimyczytacPageParser.search("<main><div class='listSearch'>$cards</div></main>")
        assertEquals(10, result.size)
        assertNull(result.first().coverUrl)
    }

    @Test
    fun genuineEmptySearchIsNotAnError() {
        assertTrue(LubimyczytacPageParser.search(
            "<main><span id='searchPaginator'>Przykro nam, niczego nie znaleźliśmy.</span></main>"
        ).isEmpty())
    }

    @Test(expected = IOException::class)
    fun blockedOrChangedPageIsAnErrorNotAnEmptySearch() {
        LubimyczytacPageParser.search("<html><title>Just a moment...</title></html>")
    }

    @Test
    fun detailsReadsBookJsonLdAndIgnoresOrganization() {
        val html = """
            <script type="application/ld+json">{"@type":"Organization","name":"Serwis"}</script>
            <script type="application/ld+json">{
                "@type":"Book","name":"Tytuł & świat","url":"$url",
                "datePublished":"2024-02-01","image":"$cover",
                "author":[{"@type":"Person","name":"Anna"},{"@type":"Person","name":"Jan"}]
            }</script>
        """.trimIndent()
        assertEquals(BookMetadata(url, "Tytuł & świat", "Anna, Jan", 2024, cover),
            LubimyczytacPageParser.details(html, url))
    }

    @Test
    fun detailsSupportsGraphSingleAuthorAndMissingYearAndCover() {
        val html = """<script type="application/ld+json">
            {"@graph":[{"@type":["Book"],"name":"Tytuł","author":{"name":"Autor"}}]}
            </script>"""
        assertEquals(BookMetadata(url, "Tytuł", "Autor"), LubimyczytacPageParser.details(html, url))
    }

    @Test(expected = IOException::class)
    fun rejectsDifferentBookDetails() {
        LubimyczytacPageParser.details(
            """<script type="application/ld+json">
                {"@type":"Book","name":"Tytuł","url":"https://lubimyczytac.pl/ksiazka/456/inny","author":{"name":"Autor"}}
                </script>""", url
        )
    }

    @Test(expected = IOException::class)
    fun malformedJsonIsReported() {
        LubimyczytacPageParser.details("<script type='application/ld+json'>{broken</script>", url)
    }

    @Test
    fun validatesOnlyHttpsBookLinksAndPublisherCoverHost() {
        assertTrue(BookMetadata.isBookUrl(url))
        listOf("http://lubimyczytac.pl/ksiazka/123/test", "https://evil.test/ksiazka/123/test",
            "https://lubimyczytac.pl.evil.test/ksiazka/123/test", "https://lubimyczytac.pl/zaloguj",
            "https://lubimyczytac.pl:123/ksiazka/123/test", "not a url").forEach {
            assertFalse(it, BookMetadata.isBookUrl(it))
        }
        assertTrue(BookMetadata.isCoverUrl(cover))
        assertFalse(BookMetadata.isCoverUrl("https://evil.test/cover.jpg"))
    }
}
