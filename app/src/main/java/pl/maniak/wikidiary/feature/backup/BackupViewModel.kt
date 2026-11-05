package pl.maniak.wikidiary.feature.backup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupViewModel(
    private val coordinator: BackupCoordinator,
    private val preferences: BackupPreferences,
    private val scheduler: BackupScheduler,
) : ViewModel() {
    val state = preferences.state
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()

    fun authorized(action: String, account: String, token: String) = perform {
        when (action) {
            "connect" -> coordinator.connect(account, token)
            "restore" -> {
                require(account == state.value.account) { "Wybrane konto zostało zmienione. Spróbuj ponownie." }
                coordinator.restore(token)
            }
            "local" -> {
                require(account == state.value.account) { "Wybrane konto zostało zmienione. Spróbuj ponownie." }
                coordinator.saveLocalCopy(token)
            }
            "save" -> {
                require(account == state.value.account) { "Wybrane konto zostało zmienione. Spróbuj ponownie." }
                coordinator.save(token)
            }
            else -> error("Nieznana operacja kopii.")
        }
        scheduler.enqueue()
    }

    fun disconnect() = perform { coordinator.disconnect() }

    fun showError(message: String) {
        Log.w("ConfigurationBackup", message)
        preferences.update { it.copy(error = message) }
    }

    private fun perform(action: suspend () -> Unit) {
        if (mutableBusy.value) return
        mutableBusy.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    preferences.update { it.copy(error = null) }
                    action()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e("ConfigurationBackup", "Backup operation failed", exception)
                showError(exception.message ?: "Nie udało się wykonać operacji kopii.")
            } finally {
                mutableBusy.value = false
            }
        }
    }
}
