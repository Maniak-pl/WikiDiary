package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.routines.domain.Routine
import pl.maniak.wikidiary.feature.routines.ui.RoutinesIntent
import pl.maniak.wikidiary.feature.routines.ui.RoutinesViewModel
import pl.maniak.wikidiary.testing.InMemoryConfig
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class RoutinesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val diaryDay = Date(1_728_480_000_000)
    private val viewModel by lazy {
        RoutinesViewModel(
            repositories.routines,
            repositories.notes,
            InMemoryConfig(),
            SelectedDateStore(diaryDay)
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun completingRoutineWritesRoutineNoteOnSelectedDay() = runBlocking {
        repositories.routines.saveRoutine(Routine(name = "Stretching"))
        val routine = repositories.routines.getRoutines().single()

        val notes = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(RoutinesIntent.SetCompleted(routine.id, completed = true))
        }

        assertEquals(
            listOf(Triple("Routine", "Wykonano rutynę \"Stretching\"", diaryDay)),
            notes.map { Triple(it.tag, it.content, it.date) }
        )
    }

    @Test
    fun routinesAreOrderedByHistoricalCompletionFrequency() = runBlocking {
        repositories.routines.saveRoutine(Routine(name = "Often"))
        repositories.routines.saveRoutine(Routine(name = "Rarely"))
        repeat(3) {
            repositories.notes.saveNote(
                WikiNote(
                    tag = "Routine",
                    content = "Wykonano rutynę \"Often\"",
                    date = diaryDay,
                )
            )
        }
        repositories.notes.saveNote(
            WikiNote(
                tag = "Routine",
                content = "Wykonano rutynę \"Rarely\"",
                date = diaryDay,
            )
        )

        assertEquals(
            listOf("Often", "Rarely"),
            repositories.routines.getRoutines().map(Routine::name)
        )
    }
}
