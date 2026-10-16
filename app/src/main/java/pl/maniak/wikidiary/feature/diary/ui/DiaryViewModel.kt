package pl.maniak.wikidiary.feature.diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonMentionResolver
import pl.maniak.wikidiary.feature.people.domain.PersonRepository
import pl.maniak.wikidiary.feature.todo.domain.AddTodo

data class DiaryUiState(
    val tags: List<Tag> = emptyList(),
    val people: List<Person> = emptyList(),
    val selectedDate: Date = Date(),
)

sealed interface DiaryIntent {
    data class AddNote(val tag: Tag, val content: String, val needsCompletion: Boolean = false) : DiaryIntent
    data class AddTag(val name: String) : DiaryIntent
    data class AddTodo(val title: String) : DiaryIntent
}

class DiaryViewModel(
    private val noteRepository: WikiNoteRepository,
    private val tagRepository: TagRepository,
    private val personRepository: PersonRepository,
    private val config: Config,
    private val selectedDate: SelectedDateStore,
    private val addTodo: AddTodo,
) : ViewModel() {
    private val noteSaveMutex = Mutex()

    val uiState: StateFlow<DiaryUiState> = combine(
        tagRepository.observeTags(),
        personRepository.observePeople(),
        selectedDate.date,
        ::DiaryUiState
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DiaryUiState(selectedDate = selectedDate.date.value)
    )

    fun onIntent(intent: DiaryIntent) {
        when (intent) {
            is DiaryIntent.AddNote -> addNote(intent.tag, intent.content, intent.needsCompletion)
            is DiaryIntent.AddTag -> addTag(intent.name)
            is DiaryIntent.AddTodo -> viewModelScope.launch { addTodo(intent.title) }
        }
    }

    private fun addNote(tag: Tag, content: String, needsCompletion: Boolean) {
        val date = selectedDate.date.value
        viewModelScope.launch {
            noteSaveMutex.withLock {
                val resolvedTag = if (tag.id == 0L) {
                    tagRepository.getTags().firstOrNull { it.name == tag.name } ?: tag
                } else {
                    tag
                }
                val people = PersonMentionResolver.resolve(content, personRepository.getPeople())
                noteRepository.saveNoteWithPeople(
                    note = WikiNote(
                        tag = resolvedTag.name,
                        content = PersonMentionResolver.replaceWithWikiLinks(content, people),
                        category = resolvedTag.category,
                        date = date,
                        needsCompletion = needsCompletion,
                        people = people
                    ),
                    personIds = people.map(Person::id)
                )
                config.setLastUpdated(System.currentTimeMillis())
                tagRepository.saveTag(resolvedTag.copy(date = Date()))
            }
        }
    }

    private fun addTag(name: String) {
        viewModelScope.launch { tagRepository.saveTag(Tag(id = 0, name = name)) }
    }
}
