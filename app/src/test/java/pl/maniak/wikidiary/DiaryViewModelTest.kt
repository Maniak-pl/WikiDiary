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
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.ui.DiaryIntent
import pl.maniak.wikidiary.feature.diary.ui.DiaryViewModel
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
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
            SelectedDateStore(diaryDay)
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
}
