package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.ui.NotesListIntent
import pl.maniak.wikidiary.feature.diary.ui.NotesListViewModel
import pl.maniak.wikidiary.feature.pictograms.data.PictogramEntryMapper
import pl.maniak.wikidiary.feature.pictograms.data.PictogramMapper
import pl.maniak.wikidiary.feature.pictograms.data.PictogramRepositoryImpl
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class NotesListViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val viewModel by lazy {
        NotesListViewModel(
            repositories.notes,
            PictogramRepositoryImpl(
                pictogramDao = database.pictogramDao(),
                pictogramEntryDao = database.pictogramEntryDao(),
                pictogramMapper = PictogramMapper(),
                pictogramEntryMapper = PictogramEntryMapper()
            )
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun setNeedsCompletionMarksAndUnmarksSelectedNotes() = runBlocking {
        val first = saveNote("Wizyta")
        val second = saveNote("Spacer")
        val untouched = saveNote("Praca")

        val marked = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(NotesListIntent.SetNeedsCompletion(listOf(first, second), needsCompletion = true))
        }
        assertEquals(listOf("Wizyta", "Spacer"), marked.filter(WikiNote::needsCompletion).map(WikiNote::content))

        val unmarked = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(NotesListIntent.SetNeedsCompletion(listOf(first), needsCompletion = false))
        }
        assertEquals(listOf("Spacer"), unmarked.filter(WikiNote::needsCompletion).map(WikiNote::content))
        assertEquals(false, unmarked.single { it.id == untouched }.needsCompletion)
    }

    private suspend fun saveNote(content: String): Long =
        repositories.notes.saveNoteWithPeople(WikiNote(tag = "Today", content = content, date = Date()), emptyList())
}
