package pl.maniak.wikidiary.feature.expenses.ui

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.composer.Capture
import pl.maniak.wikidiary.feature.diary.composer.QuickCapture
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseAmount
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseDraft
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseRepository
import pl.maniak.wikidiary.feature.expenses.domain.PaymentMethod

data class ExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.ACCOUNT,
    val tag: Tag = Tag(id = 0, name = "Finances"),
    val tags: List<Tag> = emptyList(),
    val date: Date = Date(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

sealed interface ExpenseIntent {
    data class Amount(val value: String) : ExpenseIntent
    data class Description(val value: String) : ExpenseIntent
    data class TagSelected(val tag: Tag) : ExpenseIntent
    data class PaymentSelected(val method: PaymentMethod) : ExpenseIntent
    data object Save : ExpenseIntent
}

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val tagRepository: TagRepository,
    private val config: Config,
    private val selectedDate: SelectedDateStore,
) : ViewModel() {
    private val state = MutableStateFlow(ExpenseUiState(date = selectedDate.date.value))
    private var sessionId: String? = null
    private var saveJob: Job? = null
    val uiState: StateFlow<ExpenseUiState> = state

    init {
        viewModelScope.launch {
            selectedDate.date.collect { date -> state.update { it.copy(date = date) } }
        }
    }

    suspend fun open(input: String, tagId: Long?, sessionId: String? = null) {
        if (sessionId != null && this.sessionId == sessionId && !state.value.isLoading) return
        saveJob?.join()
        this.sessionId = sessionId
        state.value = ExpenseUiState(date = selectedDate.date.value)
        try {
            val tags = tagRepository.getTags()
            val capture = QuickCapture.parseNote(input, tags)
            val taggedNote = capture as? Capture.SaveNote
            val parts = (taggedNote?.content ?: input).trim().split(Regex("\\s+"), limit = 2)
            val defaultTag = state.value.tag
            val tag = tags.firstOrNull { it.id == tagId } ?: taggedNote?.tag
                ?: tags.firstOrNull { it.name.equals(defaultTag.name, ignoreCase = true) } ?: defaultTag
            state.update {
                it.copy(
                    amount = parts.first(),
                    description = parts.getOrElse(1) { "" },
                    tag = tag,
                    tags = (tags + tag).distinctBy(Tag::name),
                    isLoading = false
                )
            }
        } catch (exception: SQLiteException) {
            state.update { it.copy(error = exception.message ?: "Nie udało się wczytać tagów.") }
        }
    }

    fun onIntent(intent: ExpenseIntent) {
        if (state.value.isLoading || state.value.isSaving || state.value.saved) return
        when (intent) {
            is ExpenseIntent.Amount -> state.update { it.copy(amount = intent.value, error = null) }
            is ExpenseIntent.Description -> state.update { it.copy(description = intent.value, error = null) }
            is ExpenseIntent.TagSelected -> state.update { it.copy(tag = intent.tag, error = null) }
            is ExpenseIntent.PaymentSelected -> state.update { it.copy(paymentMethod = intent.method, error = null) }
            ExpenseIntent.Save -> save()
        }
    }

    private fun save() {
        val form = state.value
        val amount = ExpenseAmount.parse(form.amount)
        if (amount == null) {
            state.update { it.copy(error = "Podaj dodatnią kwotę w zł, z maksymalnie 2 cyframi po przecinku.") }
            return
        }
        if (form.description.isBlank()) {
            state.update { it.copy(error = "Wpisz opis wydatku.") }
            return
        }
        state.update { it.copy(isSaving = true, error = null) }
        saveJob = viewModelScope.launch {
            try {
                repository.saveExpense(
                    ExpenseDraft(amount, form.description, form.tag, form.paymentMethod),
                    form.date
                )
                config.setLastUpdated(System.currentTimeMillis())
                state.update { it.copy(isSaving = false, saved = true) }
            } catch (exception: SQLiteException) {
                state.update {
                    it.copy(isSaving = false, error = exception.message ?: "Nie udało się zapisać wydatku.")
                }
            }
        }
    }
}
