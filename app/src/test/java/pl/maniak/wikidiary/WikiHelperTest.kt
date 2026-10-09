package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.domain.model.FilmMediaType
import pl.maniak.wikidiary.domain.model.FilmMetadata
import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.WikiHelper
import java.text.SimpleDateFormat
import java.util.Locale

class WikiHelperTest {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    @Test
    fun preparingEntryOnWiki_keepsGroupsAndDeduplicatesImagesPerDay() {
        val filmUrl = "https://www.filmweb.pl/film/example-2020"
        val firstNote = WikiNote(
            id = 1,
            tag = "Today",
            content = "Obejrzałem film [[$filmUrl|Example (2020)]]",
            date = dateFormat.parse("2024-10-09 18:00")!!
        )
        val secondNote = firstNote.copy(
            id = 2,
            tag = "Today",
            content = "Obejrzałem film [[$filmUrl|Example (2020)]]",
            date = dateFormat.parse("2024-10-09 21:00")!!
        )
        val metadata = FilmMetadata(
            canonicalUrl = filmUrl,
            title = "Example",
            year = 2020,
            mediaType = FilmMediaType.FILM
        )

        val result = WikiHelper.preparingEntryOnWiki(
            noteList = listOf(secondNote, firstNote),
            filmNotes = listOf(
                FilmNoteEntry(noteId = 1, metadata = metadata),
                FilmNoteEntry(noteId = 2, metadata = metadata)
            )
        )

        assertEquals(1, result.split("{{ :movies:Example_2020.jpg?200 |}}").size - 1)
        assertEquals(2, result.split("[[$filmUrl|Example (2020)]]").size - 1)
        assertTrue(result.indexOf("{{ :movies:Example_2020.jpg?200 |}}") > result.lastIndexOf("* **Today**"))
    }

    @Test
    fun preparingEntryOnWiki_avoidsImageNameCollisionForDifferentUrls() {
        val firstUrl = "https://www.filmweb.pl/film/example-2020"
        val secondUrl = "https://www.filmweb.pl/film/example-remake-2020"
        val firstNote = WikiNote(
            id = 1,
            tag = "Today",
            content = "First",
            date = dateFormat.parse("2024-10-09 18:00")!!
        )
        val secondNote = firstNote.copy(id = 2, content = "Second")
        val firstMetadata = FilmMetadata(
            canonicalUrl = firstUrl,
            title = "Example",
            year = 2020,
            mediaType = FilmMediaType.FILM
        )
        val secondMetadata = firstMetadata.copy(canonicalUrl = secondUrl)

        val result = WikiHelper.preparingEntryOnWiki(
            noteList = listOf(firstNote, secondNote),
            filmNotes = listOf(
                FilmNoteEntry(noteId = 1, metadata = firstMetadata),
                FilmNoteEntry(noteId = 2, metadata = secondMetadata)
            )
        )

        assertTrue(result.contains("{{ :movies:Example_2020.jpg?200 |}}"))
        assertTrue(result.contains("{{ :movies:Example_2020_2.jpg?200 |}}"))
    }
}
