package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.feature.filmweb.domain.FilmMediaType
import pl.maniak.wikidiary.feature.filmweb.domain.FilmMetadata
import pl.maniak.wikidiary.feature.filmweb.domain.FilmNoteEntry
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.domain.NotesExportContributor
import pl.maniak.wikidiary.feature.filmweb.domain.FilmPosterExportContributor
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportItem
import pl.maniak.wikidiary.feature.export.domain.WikiExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import java.text.SimpleDateFormat
import java.util.Locale

class WikiExporterTest {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    @Test
    fun export_keepsGroupsAndDeduplicatesImagesPerDay() {
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

        val result = export(
            notes = listOf(secondNote, firstNote),
            films = listOf(
                FilmNoteEntry(noteId = 1, metadata = metadata),
                FilmNoteEntry(noteId = 2, metadata = metadata)
            )
        )

        assertEquals(1, result.split("{{ :movies:Example_2020.jpg?200 |}}").size - 1)
        assertEquals(2, result.split("[[$filmUrl|Example (2020)]]").size - 1)
        assertTrue(result.indexOf("{{ :movies:Example_2020.jpg?200 |}}") > result.lastIndexOf("* **Today**"))
    }

    @Test
    fun export_avoidsImageNameCollisionForDifferentUrls() {
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

        val result = export(
            notes = listOf(firstNote, secondNote),
            films = listOf(
                FilmNoteEntry(noteId = 1, metadata = firstMetadata),
                FilmNoteEntry(noteId = 2, metadata = secondMetadata)
            )
        )

        assertTrue(result.contains("{{ :movies:Example_2020.jpg?200 |}}"))
        assertTrue(result.contains("{{ :movies:Example_2020_2.jpg?200 |}}"))
    }

    @Test
    fun export_keepsPictogramsUnderTheirOwnTagGroup() {
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

        val result = export(
            notes = listOf(todayNote, workNote),
            pictograms = listOf(
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
    fun export_replacesPeopleMentionsInline() {
        val note = WikiNote(
            tag = "Today",
            content = "Rozmowa zespołu z @Dorota Płachno i @Damian Hanusiak",
            date = dateFormat.parse("2024-10-09 18:00")!!,
            people = listOf(
                Person(1, "Dorota Płachno", PersonGender.FEMALE),
                Person(2, "Damian Hanusiak", PersonGender.MALE)
            )
        )

        val result = export(listOf(note))

        assertTrue(
            result.contains(
                "* Rozmowa zespołu z **🙍🏻‍♀️ [[:Community:Dorota Płachno]]** " +
                    "i **🙍🏻‍♂️ [[:Community:Damian Hanusiak]]**"
            )
        )
        assertFalse(result.contains("\n**🙍🏻‍♀️ [[:Community:Dorota Płachno]]**"))
    }

    @Test
    fun export_rendersDaysGroupsPictogramsAndPosters() {
        val notes = listOf(
            WikiNote(
                id = 3,
                tag = "Work",
                content = "Release",
                category = "WikiDiary",
                date = dateFormat.parse("2024-10-10 09:00")!!
            ),
            WikiNote(
                id = 1,
                tag = "Today",
                content = "Obejrzałem film",
                date = dateFormat.parse("2024-10-09 18:00")!!
            ),
            WikiNote(
                id = 2,
                tag = "Work",
                content = "Meeting",
                date = dateFormat.parse("2024-10-09 19:00")!!
            )
        )
        val films = listOf(
            FilmNoteEntry(
                noteId = 1,
                metadata = FilmMetadata(
                    canonicalUrl = "https://www.filmweb.pl/film/Diuna-2021-232796",
                    title = "Diuna",
                    year = 2021,
                    mediaType = FilmMediaType.FILM
                )
            )
        )
        val pictograms = listOf(
            PictogramEntry(
                id = 1,
                pictogramId = 1,
                tag = "Today",
                date = dateFormat.parse("2024-10-09 20:00")!!,
                name = "love",
                icon = "❤️"
            ),
            PictogramEntry(
                id = 2,
                pictogramId = 2,
                tag = "Work",
                category = "WikiDiary",
                date = dateFormat.parse("2024-10-10 10:00")!!,
                name = "meeting",
                icon = "🤝"
            )
        )

        val result = export(notes = notes, films = films, pictograms = pictograms)

        assertEquals(
            """
            |===== 09.10.2024, Wednesday =====
            |
            |  * **Today**
            |    * Obejrzałem film
            |{{ :pictograms_-_love.png?200 |}}
            |
            |  * **Work**
            |    * Meeting
            |
            |{{ :movies:Diuna_2021.jpg?200 |}}
            |
            |===== 10.10.2024, Thursday =====
            |
            |  * **📓 [[:WikiDiary]] - [[:WikiDiary:Projekt - Work 2024]]**
            |    * Release
            |{{ :pictograms_-_meeting.png?200 |}}
            |
            |""".trimMargin(),
            result
        )
    }

    @Test
    fun export_includesLinesFromAdditionalContributorUnderItsGroup() {
        val date = dateFormat.parse("2024-10-09 18:00")!!
        val note = WikiNote(id = 1, tag = "Today", content = "Worked", date = date)
        val todoContributor = WikiExportContributor {
            flowOf(
                listOf(
                    WikiExportItem.GroupLine(date = date, group = "Today", line = "    * Zadanie zamknięte"),
                    WikiExportItem.GroupLine(date = date, group = "ToDo", line = "    * Nowe zadanie")
                )
            )
        }

        val result = runBlocking {
            WikiExporter(listOf(NotesExportContributor(flowOf(listOf(note))), todoContributor))
                .document()
                .first()
        }

        assertEquals(
            """
            |===== 09.10.2024, Wednesday =====
            |
            |  * **Today**
            |    * Worked
            |    * Zadanie zamknięte
            |
            |  * **ToDo**
            |    * Nowe zadanie
            |
            |""".trimMargin(),
            result
        )
    }

    private fun export(
        notes: List<WikiNote>,
        films: List<FilmNoteEntry> = emptyList(),
        pictograms: List<PictogramEntry> = emptyList()
    ): String = runBlocking {
        WikiExporter(
            listOf(
                FilmPosterExportContributor(flowOf(notes), flowOf(films)),
                PictogramExportContributor(flowOf(pictograms)),
                NotesExportContributor(flowOf(notes))
            )
        ).document().first()
    }
}
