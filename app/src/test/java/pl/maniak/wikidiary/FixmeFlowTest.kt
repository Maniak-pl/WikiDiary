package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.composer.Capture
import pl.maniak.wikidiary.feature.diary.composer.QuickCapture
import pl.maniak.wikidiary.feature.diary.domain.NotesExportContributor
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.ui.DiaryIntent
import pl.maniak.wikidiary.feature.diary.ui.DiaryViewModel
import pl.maniak.wikidiary.feature.export.domain.WikiExporter
import pl.maniak.wikidiary.feature.todo.domain.AddTodo
import pl.maniak.wikidiary.testing.InMemoryConfig
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

/** Composer text with the completion command → saved note → wiki export: only the wiki marker is exported. */
@RunWith(RobolectricTestRunner::class)
class FixmeFlowTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val work = Tag(id = 0, name = "Work")
    private val viewModel by lazy {
        DiaryViewModel(
            repositories.notes,
            repositories.tags,
            repositories.people,
            InMemoryConfig(),
            SelectedDateStore(Date(1_728_480_000_000)),
            AddTodo(repositories.todos, repositories.notes, InMemoryConfig())
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun fixmeCommandTypedBeforeNoteIsExportedAsFixme() =
        assertSavedAndExportedAsFixme(QuickCapture.parse("/fixme Wizyta u lekarza #Work", listOf(work)))

    @Test
    fun fixmeWordTypedAfterNoteIsExportedAsFixme() =
        assertSavedAndExportedAsFixme(QuickCapture.parse("Wizyta u lekarza /fixme #Work", listOf(work)))

    @Test
    fun fixmeWithClickedTagIsExportedAsFixme() =
        assertSavedAndExportedAsFixme(QuickCapture.parseForTag("Wizyta u lekarza /fixme", work))

    private fun assertSavedAndExportedAsFixme(capture: Capture) = runBlocking {
        val note = save(capture as Capture.SaveNote)

        assertEquals("Wizyta u lekarza", note.content)
        assertTrue(note.needsCompletion)

        val export = WikiExporter(listOf(NotesExportContributor(repositories.notes.observeNotes())))
            .document()
            .first()
        assertTrue(export, export.contains("    * Wizyta u lekarza FIXME\n"))
        assertFalse(export, export.contains("/fixme", ignoreCase = true))
    }

    private suspend fun save(capture: Capture.SaveNote): WikiNote =
        repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(DiaryIntent.AddNote(capture.tag, capture.content, capture.needsCompletion))
        }.single()
}
