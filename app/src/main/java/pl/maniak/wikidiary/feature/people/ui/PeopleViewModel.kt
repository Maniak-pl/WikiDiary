package pl.maniak.wikidiary.feature.people.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import pl.maniak.wikidiary.feature.people.domain.PersonRepository

data class PeopleUiState(
    val people: List<Person> = emptyList(),
    val error: String? = null,
)

sealed interface PeopleIntent {
    data class Add(val name: String, val gender: PersonGender) : PeopleIntent
    data class Delete(val id: Long) : PeopleIntent
    data object DismissError : PeopleIntent
}

class PeopleViewModel(
    private val personRepository: PersonRepository,
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PeopleUiState> = combine(
        personRepository.observePeople(),
        error,
        ::PeopleUiState
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeopleUiState())

    fun onIntent(intent: PeopleIntent) {
        when (intent) {
            is PeopleIntent.Add -> add(intent.name, intent.gender)
            is PeopleIntent.Delete -> viewModelScope.launch { personRepository.deletePerson(intent.id) }
            PeopleIntent.DismissError -> error.value = null
        }
    }

    private fun add(name: String, gender: PersonGender) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            error.value = "Podaj imię i nazwisko."
            return
        }
        viewModelScope.launch {
            if (personRepository.getPeople().any { it.name.equals(normalizedName, ignoreCase = true) }) {
                error.value = "Ta osoba jest już zapisana."
                return@launch
            }
            personRepository.savePerson(Person(name = normalizedName, gender = gender))
            error.value = null
        }
    }
}
