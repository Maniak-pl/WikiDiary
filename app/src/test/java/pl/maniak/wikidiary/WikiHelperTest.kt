package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.domain.model.FilmMediaType
import pl.maniak.wikidiary.domain.model.FilmMetadata
import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.PictogramEntry
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.WikiHelper
import pl.maniak.wikidiary.utils.helpers.WikiParser
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

    @Test
    fun preparingEntryOnWiki_keepsPictogramsUnderTheirOwnTagGroup() {
        val firstDate = dateFormat.parse("2024-10-09 18:00")!!
        val secondDate = dateFormat.parse("2024-10-09 21:00")!!
        val todayNote = WikiNote(
            id = 1,
            tag = "Today",
            content = "Worked on WikiDiary",
            date = firstDate
        )
        val workNote = WikiNote(
            id = 2,
            tag = "Work",
            content = "Had a meeting",
            date = secondDate
        )

        val result = WikiHelper.preparingEntryOnWiki(
            noteList = listOf(todayNote, workNote),
            pictogramEntries = listOf(
                PictogramEntry(
                    id = 1,
                    pictogramId = 1,
                    tag = "Today",
                    date = firstDate,
                    name = "love",
                    icon = "❤️"
                ),
                PictogramEntry(
                    id = 2,
                    pictogramId = 2,
                    tag = "Today",
                    date = firstDate,
                    name = "doctor",
                    icon = "🩺"
                ),
                PictogramEntry(
                    id = 3,
                    pictogramId = 3,
                    tag = "Work",
                    date = secondDate,
                    name = "meeting",
                    icon = "🤝"
                )
            )
        )

        val todayGroup = result.indexOf("* **Today**")
        val workGroup = result.indexOf("* **Work**")
        val todayNoteIndex = result.indexOf("* Worked on WikiDiary")
        val loveImage = result.indexOf("{{ :pictograms_-_love.png?200 |}}")
        val doctorImage = result.indexOf("{{ :pictograms_-_doctor.png?200 |}}")
        val meetingImage = result.indexOf("{{ :pictograms_-_meeting.png?200 |}}")

        assertTrue(todayGroup >= 0)
        assertTrue(todayNoteIndex in todayGroup until loveImage)
        assertTrue(loveImage < doctorImage)
        assertTrue(doctorImage < workGroup)
        assertTrue(workGroup < meetingImage)
        assertEquals(1, result.split("{{ :pictograms_-_love.png?200 |}}").size - 1)
        assertEquals(1, result.split("{{ :pictograms_-_doctor.png?200 |}}").size - 1)
        assertEquals(1, result.split("{{ :pictograms_-_meeting.png?200 |}}").size - 1)
    }

    @Test
    fun pictogramName_generatesDokuWikiFileReference() {
        assertEquals(
            "{{ :pictograms_-_love.png?200 |}}",
            WikiParser.addPictogram("love")
        )
        assertEquals(
            "{{ :pictograms_-_spacer_na_dzien.png?200 |}}",
            WikiParser.addPictogram(" Späcer na dzień ")
        )
    }
}
