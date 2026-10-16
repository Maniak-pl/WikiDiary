package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.composer.Capture
import pl.maniak.wikidiary.feature.diary.composer.QuickCapture
import pl.maniak.wikidiary.feature.diary.domain.NotesExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExporter
import pl.maniak.wikidiary.feature.diary.ui.DiaryIntent
import pl.maniak.wikidiary.feature.diary.ui.DiaryViewModel
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import pl.maniak.wikidiary.feature.todo.domain.AddTodo
import pl.maniak.wikidiary.testing.InMemoryConfig
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class DiaryViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val diaryDay = Date(1_728_480_000_000)
    private val viewModel by lazy {
        DiaryViewModel(
            repositories.notes,
            repositories.tags,
            repositories.people,
            InMemoryConfig(),
            SelectedDateStore(diaryDay),
            AddTodo(repositories.todos, repositories.notes, InMemoryConfig())
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun savedNoteLinksMentionedPersonOnSelectedDay() = runBlocking {
        repositories.people.savePerson(Person(name = "Martyn Pekala", gender = PersonGender.MALE))

        val note = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(
                DiaryIntent.AddNote(
                    tag = Tag(id = 0, name = "Work", category = "Projects"),
                    content = "Umwowlem sie z @Martyn Pekala na spotaknie"
                )
            )
        }.single()

        assertEquals("Work", note.tag)
        assertEquals("Projects", note.category)
        assertEquals(diaryDay, note.date)
        assertEquals(
            "Umwowlem sie z **🙍🏻‍♂️ [[:Community:Martyn Pekala]]** na spotaknie",
            note.content
        )
        assertEquals(listOf("Martyn Pekala"), note.people.map(Person::name))
    }

    @Test
    fun noteMarkedForCompletionIsSavedWithTheMarker() = runBlocking {
        val note = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(
                DiaryIntent.AddNote(tag = Tag(id = 0, name = "Today"), content = "Wizyta", needsCompletion = true)
            )
        }.single()

        assertEquals(true, note.needsCompletion)
    }

    @Test
    fun reflectionCommandsCreateGroupsOnceAndExportOnSelectedDayWithMentions() = runBlocking {
        repositories.people.savePerson(Person(name = "Jan Nowak", gender = PersonGender.MALE))
        val inputs = listOf(
            "/mysl Warto zwolnić /fixme",
            "/mysl Druga myśl",
            "/wdzieczny Za spacer z @Jan Nowak",
            "/wdzieczny Za odpoczynek"
        )
        val captures = inputs.map { QuickCapture.parse(it, emptyList()) }
        captures.forEach { capture ->
            check(capture is Capture.SaveNote)
            viewModel.onIntent(DiaryIntent.AddNote(capture.tag, capture.content, capture.needsCompletion))
        }
        val notes = withTimeout(5_000) { repositories.notes.observeNotes().first { it.size == 4 } }
        val tags = withTimeout(5_000) { repositories.tags.observeTags().first { it.size == 2 } }
        assertEquals(setOf("Przemyślenia", "Kronika wdzięczności"), tags.map { it.name }.toSet())
        assertTrue(notes.all { it.date == diaryDay })
        assertTrue(notes.first().needsCompletion)
        assertEquals(listOf("Jan Nowak"), notes[2].people.map(Person::name))

        val export = WikiExporter(listOf(NotesExportContributor(repositories.notes.observeNotes())))
            .document().first()
        assertTrue(export.contains("  * **Przemyślenia**"))
        assertTrue(export.contains("  * **Kronika wdzięczności**"))
        assertTrue(export.contains("    * Warto zwolnić FIXME\n"))
        assertTrue(export.contains("    * Druga myśl\n"))
        assertTrue(export.contains("[[:Community:Jan Nowak]]"))
        assertTrue(export.contains("    * Za odpoczynek\n"))
        assertFalse(export.contains("/mysl"))
        assertFalse(export.contains("/wdzieczny"))
    }

    @Test
    fun clickedTagPathReusesExistingReflectionGroupInsteadOfCreatingDuplicate() = runBlocking {
        repositories.tags.saveTag(Tag(0, "Przemyślenia"))
        val existingTag = repositories.tags.getTags().single()
        val capture = QuickCapture.parseForTag("/mysl Warto zwolnić", Tag(99, "Work"))
        check(capture is Capture.SaveNote)
        val note = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(DiaryIntent.AddNote(capture.tag, capture.content, capture.needsCompletion))
        }.single()
        assertEquals("Przemyślenia", note.tag)
        assertEquals("Warto zwolnić", note.content)
        assertEquals(existingTag.id, repositories.tags.getTags().single().id)
    }

    @Test
    fun todoCommandAddsTaskAndLogsItUnderToDoTag() = runBlocking {
        val note = repositories.notes.observeNotes().nextValueAfter {
            viewModel.onIntent(DiaryIntent.AddTodo("Umyć garki"))
        }.single()

        assertEquals(listOf("Umyć garki"), repositories.todos.getTodos().map { it.title })
        assertEquals("ToDo", note.tag)
        assertEquals("Dodano zadanie: Umyć garki", note.content)
    }
}
