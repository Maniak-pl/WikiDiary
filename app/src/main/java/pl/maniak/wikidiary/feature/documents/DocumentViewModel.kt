package pl.maniak.wikidiary.feature.documents

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.diary.domain.CategoryRepository

data class DocumentStatus(
    val busy: Boolean = false,
    val error: String? = null,
    val pending: DocumentDraft? = null,
    val saved: Boolean = false,
)

class DocumentViewModel(
    private val drive: DriveClient,
    private val store: DocumentStore,
    tags: TagRepository,
    categories: CategoryRepository,
    private val config: Config,
    selectedDate: SelectedDateStore,
) : ViewModel() {
    val tags = tags.observeTags().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = categories.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val date = selectedDate.date
    private val mutableStatus = MutableStateFlow(DocumentStatus())
    val status = mutableStatus.asStateFlow()

    init {
        perform { mutableStatus.value = mutableStatus.value.copy(pending = store.pending()) }
    }

    fun acknowledgeSaved() {
        mutableStatus.value = mutableStatus.value.copy(saved = false)
    }

    fun abandonPending() = perform {
        store.setPending(null)
        mutableStatus.value = mutableStatus.value.copy(pending = null)
    }

    fun error(message: String) {
        Log.w("Documents", message)
        mutableStatus.value = mutableStatus.value.copy(error = message, busy = false)
    }

    fun upload(token: String, draft: DocumentDraft?) = perform {
        var pending = mutableStatus.value.pending ?: requireNotNull(draft) { "Wybierz dokument." }
        pending.validate()
        if (pending.driveId.isEmpty()) {
            pending = pending.copy(driveId = drive.generateId(token))
            store.setPending(pending)
            mutableStatus.value = mutableStatus.value.copy(pending = pending)
        }
        drive.upload(token, pending)
        store.saveNote(pending)
        config.setLastUpdated(System.currentTimeMillis())
        store.setPending(null)
        mutableStatus.value = mutableStatus.value.copy(pending = null, saved = true)
    }

    private fun perform(action: suspend () -> Unit) {
        if (mutableStatus.value.busy) return
        mutableStatus.value = mutableStatus.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { action() }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e("Documents", "Document operation failed", exception)
                mutableStatus.value = mutableStatus.value.copy(
                    error = exception.message ?: "Nie udało się zapisać dokumentu."
                )
            } finally {
                mutableStatus.value = mutableStatus.value.copy(busy = false)
            }
        }
    }
}
