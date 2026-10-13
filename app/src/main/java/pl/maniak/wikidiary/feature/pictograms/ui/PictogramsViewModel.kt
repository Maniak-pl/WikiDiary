package pl.maniak.wikidiary.feature.pictograms.ui

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.pictograms.domain.Pictogram
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramRepository

data class PictogramsUiState(
    val pictograms: List<Pictogram> = emptyList(),
    val entries: List<PictogramEntry> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedDate: Date = Date(),
    val error: String? = null,
)

sealed interface PictogramIntent {
    data class Save(val id: Long, val name: String, val icon: String) : PictogramIntent
    data class Delete(val id: Long) : PictogramIntent
    data class AttachToTag(val pictogramId: Long, val tag: Tag, val date: Date) : PictogramIntent
    data object DismissError : PictogramIntent
}

class PictogramsViewModel(
    private val pictogramRepository: PictogramRepository,
    tagRepository: TagRepository,
    selectedDate: SelectedDateStore,
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PictogramsUiState> = combine(
        pictogramRepository.observePictograms(),
        pictogramRepository.observePictogramEntries(),
        tagRepository.observeTags(),
        selectedDate.date,
        error,
        ::PictogramsUiState
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PictogramsUiState(selectedDate = selectedDate.date.value)
    )

    fun onIntent(intent: PictogramIntent) {
        when (intent) {
            is PictogramIntent.Save -> save(intent)
            is PictogramIntent.Delete -> viewModelScope.launch {
                pictogramRepository.deletePictogram(intent.id)
            }
            is PictogramIntent.AttachToTag -> attachToTag(intent)
            PictogramIntent.DismissError -> error.value = null
        }
    }

    private fun save(intent: PictogramIntent.Save) {
        val name = intent.name.trim()
        val icon = intent.icon.trim()
        if (name.isBlank()) {
            error.value = "Podaj nazwę pictogramu."
            return
        }
        if (icon.isBlank()) {
            error.value = "Wybierz ikonę pictogramu."
            return
        }
        viewModelScope.launch {
            val fileName = WikiParser.pictogramFileName(name)
            val hasFileNameCollision = pictogramRepository.getPictograms().any {
                it.id != intent.id && WikiParser.pictogramFileName(it.name) == fileName
            }
            if (hasFileNameCollision) {
                error.value = "Ta nazwa generuje już istniejący plik pictogramu."
                return@launch
            }
            try {
                pictogramRepository.savePictogram(Pictogram(id = intent.id, name = name, icon = icon))
                error.value = null
            } catch (exception: SQLiteException) {
                error.value = if (intent.id == 0L) {
                    "Pictogram o tej nazwie już istnieje."
                } else {
                    exception.message ?: "Nie udało się zapisać pictogramu."
                }
            }
        }
    }

    private fun attachToTag(intent: PictogramIntent.AttachToTag) {
        viewModelScope.launch {
            val pictogram = pictogramRepository.getPictograms().firstOrNull { it.id == intent.pictogramId }
            if (pictogram == null) {
                error.value = "Wybrany pictogram nie jest już dostępny."
                return@launch
            }
            try {
                pictogramRepository.addPictogramEntry(
                    PictogramEntry(
                        pictogramId = pictogram.id,
                        tag = intent.tag.name,
                        category = intent.tag.category,
                        date = intent.date,
                        name = pictogram.name,
                        icon = pictogram.icon
                    )
                )
                error.value = null
            } catch (exception: SQLiteException) {
                error.value = exception.message ?: "Nie udało się przypisać pictogramu."
            }
        }
    }
}
