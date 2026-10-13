package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.diary.data.CategoryMapper
import pl.maniak.wikidiary.feature.diary.data.CategoryRepositoryImpl
import pl.maniak.wikidiary.feature.diary.data.TagMapperImpl
import pl.maniak.wikidiary.feature.diary.data.TagRepositoryImpl
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapperImpl
import pl.maniak.wikidiary.feature.diary.domain.Category
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.filmweb.data.FilmRepositoryImpl
import pl.maniak.wikidiary.feature.filmweb.domain.FilmMediaType
import pl.maniak.wikidiary.feature.filmweb.domain.FilmMetadata
import pl.maniak.wikidiary.feature.routines.data.RoutineMapper
import pl.maniak.wikidiary.feature.routines.data.RoutineRepositoryImpl
import pl.maniak.wikidiary.feature.routines.domain.Routine
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class ReactiveRepositoriesTest {

    private val database = inMemoryDatabase()

    @After
    fun tearDown() = database.close()

    @Test
    fun observedRoutinesReflectSavedRoutine() = runBlocking {
        val repository = RoutineRepositoryImpl(RoutineMapper(), database.routineDao())

        val routines = repository.observeRoutines().nextValueAfter {
            repository.saveRoutine(Routine(name = "Stretching"))
        }

        assertEquals(listOf("Stretching"), routines.map(Routine::name))
    }

    @Test
    fun observedTagsReflectSavedTag() = runBlocking {
        val repository = TagRepositoryImpl(TagMapperImpl(), database.tagDao())

        val tags = repository.observeTags().nextValueAfter {
            repository.saveTag(Tag(id = 0, name = "Work"))
        }

        assertEquals(listOf("Work"), tags.map(Tag::name))
    }

    @Test
    fun observedCategoriesReflectSavedCategory() = runBlocking {
        val repository = CategoryRepositoryImpl(CategoryMapper(), database.categoryDao())

        val categories = repository.observeCategories().nextValueAfter {
            repository.saveCategory(Category(id = 0, name = "Allegro"))
        }

        assertEquals(listOf("Allegro"), categories.map(Category::name))
    }

    @Test
    fun observedFilmNotesReflectSavedFilmNote() = runBlocking {
        val repository = FilmRepositoryImpl(
            database = database,
            wikiNoteDao = database.wikiNoteDao(),
            filmDao = database.filmDao(),
            filmNoteDao = database.filmNoteDao(),
            wikiNoteMapper = WikiNoteMapperImpl()
        )
        val metadata = FilmMetadata(
            canonicalUrl = "https://www.filmweb.pl/film/Diuna-2021-232796",
            title = "Diuna",
            year = 2021,
            mediaType = FilmMediaType.FILM
        )

        val filmNotes = repository.observeFilmNotes().nextValueAfter {
            repository.saveFilmNote(
                WikiNote(tag = "Today", content = "Obejrzałem Diunę", date = Date()),
                metadata
            )
        }

        assertEquals(listOf(metadata), filmNotes.map { it.metadata })
    }
}
