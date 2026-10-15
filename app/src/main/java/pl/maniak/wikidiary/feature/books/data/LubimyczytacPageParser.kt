package pl.maniak.wikidiary.feature.books.data

import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jsoup.Jsoup
import pl.maniak.wikidiary.feature.books.domain.BookMetadata

object LubimyczytacPageParser {
    private const val BASE_URL = "https://lubimyczytac.pl"

    fun search(html: String): List<BookMetadata> {
        val document = Jsoup.parse(html, BASE_URL)
        val results = document.select("main .listSearch .book-card")
        if (results.isEmpty()) {
            if (document.selectFirst("main #searchPaginator")?.text()
                    ?.contains("niczego nie znaleźliśmy", ignoreCase = true) == true
            ) return emptyList()
            throw IOException("Nie rozpoznano wyników Lubimyczytać.pl. Serwis mógł zmienić stronę lub zablokować dostęp.")
        }
        return results.take(10).map { card ->
            val link = card.selectFirst("a.book-card__title")
                ?: throw IOException("Lubimyczytać.pl: brak linku do książki w wynikach.")
            BookMetadata(
                canonicalUrl = link.absUrl("href"),
                title = link.text(),
                author = card.select(".book-card__author a").joinToString(", ") { it.text() },
                coverUrl = card.selectFirst("img.book-card__cover-image")?.absUrl("src")
                    ?.takeIf(BookMetadata::isCoverUrl)
            ).also {
                if (!it.isValid()) throw IOException("Lubimyczytać.pl zwróciło niekompletne dane książki.")
            }
        }.distinctBy(BookMetadata::canonicalUrl)
    }

    fun details(html: String, url: String): BookMetadata {
        val document = Jsoup.parse(html, BASE_URL)
        val books = document.select("script[type=application/ld+json]").flatMap { script ->
            val json = try {
                Json.parseToJsonElement(script.data())
            } catch (exception: SerializationException) {
                throw IOException("Nieprawidłowe metadane Lubimyczytać.pl.", exception)
            } catch (exception: IllegalArgumentException) {
                throw IOException("Nieprawidłowe metadane Lubimyczytać.pl.", exception)
            }
            objects(json).filter { obj ->
                val type = obj["@type"]
                (type as? JsonPrimitive)?.content == "Book" ||
                    (type as? JsonArray)?.any { (it as? JsonPrimitive)?.content == "Book" } == true
            }
        }
        val book = books.singleOrNull()
            ?: throw IOException("Nie znaleziono metadanych książki w Lubimyczytać.pl.")
        val authors = when (val author = book["author"]) {
            is JsonArray -> author.mapNotNull { (it as? JsonObject)?.string("name") }
            is JsonObject -> listOfNotNull(author.string("name"))
            else -> emptyList()
        }
        return BookMetadata(
            canonicalUrl = book.string("url") ?: url,
            title = book.string("name").orEmpty(),
            author = authors.joinToString(", "),
            year = book.string("datePublished")?.take(4)?.toIntOrNull()?.takeIf { it in 1000..9999 },
            coverUrl = book.string("image")?.takeIf(BookMetadata::isCoverUrl)
        ).also {
            if (!it.isValid()) throw IOException("Lubimyczytać.pl zwróciło niekompletne dane książki.")
            if (URI_BOOK_ID.find(it.canonicalUrl)?.value != URI_BOOK_ID.find(url)?.value) {
                throw IOException("Lubimyczytać.pl zwróciło dane innej książki.")
            }
        }
    }

    private val URI_BOOK_ID = Regex("/ksiazka/[0-9]+/")

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeUnless { it.content == "null" }?.content

    private fun objects(element: JsonElement): List<JsonObject> = when (element) {
        is JsonArray -> element.flatMap(::objects)
        is JsonObject -> listOf(element) + (element["@graph"]?.let(::objects) ?: emptyList())
        else -> emptyList()
    }
}
