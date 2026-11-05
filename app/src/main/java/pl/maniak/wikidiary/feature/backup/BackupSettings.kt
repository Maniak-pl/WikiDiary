package pl.maniak.wikidiary.feature.backup

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun BackupSettings(viewModel: BackupViewModel = koinViewModel()) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val busy by viewModel.busy.collectAsState()
    var action by rememberSaveable { mutableStateOf("") }
    var account by rememberSaveable { mutableStateOf("") }
    var authorizing by rememberSaveable { mutableStateOf(false) }
    var confirmRestore by rememberSaveable { mutableStateOf(false) }
    var confirmLocal by rememberSaveable { mutableStateOf(false) }
    val authorization = remember(context) { Identity.getAuthorizationClient(context) }
    val locked = busy || authorizing

    fun accept(result: AuthorizationResult) {
        authorizing = false
        val token = result.accessToken
        if (token.isNullOrBlank() || BACKUP_SCOPE !in result.grantedScopes) {
            viewModel.showError("Google nie udzielił dostępu do kopii aplikacji.")
        } else viewModel.authorized(action, account, token)
    }

    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            authorizing = false
            viewModel.showError("Anulowano dostęp do kopii Google.")
        } else {
            try {
                accept(authorization.getAuthorizationResultFromIntent(result.data))
            } catch (exception: Exception) {
                authorizing = false
                viewModel.showError("Błąd zgody Google: ${exception.message}")
            }
        }
    }

    fun authorize() {
        authorizing = true
        authorization.authorize(backupAuthorizationRequest(account))
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val intent = result.pendingIntent
                    if (intent != null) consent.launch(IntentSenderRequest.Builder(intent).build())
                    else {
                        authorizing = false
                        viewModel.showError("Google nie zwrócił ekranu zgody.")
                    }
                } else accept(result)
            }
            .addOnFailureListener {
                authorizing = false
                viewModel.showError("Nie udało się uzyskać dostępu Google. Sprawdź połączenie i spróbuj ponownie.")
            }
    }

    val chooser = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val selected = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (result.resultCode == Activity.RESULT_OK && !selected.isNullOrBlank()) {
            account = selected
            action = "connect"
            authorize()
        } else {
            authorizing = false
            viewModel.showError("Nie wybrano konta Google.")
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Kopia stałych danych na Google", style = MaterialTheme.typography.titleMedium)
        Text("Tagi, projekty, kategorie, ToDo, rutyny, pictogramy i osoby. Bez notatek i dziennych odhaczeń. Kopia dla jednego urządzenia.")
        if (state.account.isNotEmpty()) Text("Konto: ${state.account}")
        if (state.lastSaved > 0) Text("Kopia: ${DateFormat.getDateTimeInstance().format(Date(state.lastSaved))}")
        Text(when {
            state.restoreAvailable -> "Znaleziono kopię. Przywróć dane albo świadomie zapisz lokalne stałe jako nową kopię."
            state.enabled && state.dirty -> "Zmiany oczekują na zapis. Nie odinstalowuj jeszcze aplikacji."
            state.enabled -> "Automatyczna kopia włączona."
            else -> "Automatyczna kopia wyłączona."
        })
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (locked) CircularProgressIndicator()
        TextButton(enabled = !locked, onClick = {
            authorizing = true
            chooser.launch(AccountManager.newChooseAccountIntent(
                null, null, arrayOf("com.google"), null, null, null, null))
        }) { Text(if (state.account.isEmpty()) "Połącz konto Google" else "Sprawdź / zmień konto") }
        if (state.account.isNotEmpty()) {
            Button(enabled = !locked && state.enabled, onClick = {
                action = "save"
                account = state.account
                authorize()
            }) { Text("Zapisz teraz / zatwierdź dostęp") }
            if (!state.enabled && !state.restoreAvailable) {
                TextButton(enabled = !locked, onClick = {
                    action = "connect"
                    account = state.account
                    authorize()
                }) { Text("Wznów kopię") }
            }
            if (state.restoreAvailable) {
                TextButton(enabled = !locked, onClick = { confirmLocal = true }) {
                    Text("Zapisz lokalne stałe jako nową kopię")
                }
            }
            TextButton(enabled = !locked, onClick = { confirmRestore = true }) { Text("Przywróć kopię") }
            TextButton(enabled = !locked, onClick = { viewModel.disconnect() }) { Text("Wyłącz kopię na tym urządzeniu") }
        }
        Text("Po reinstalacji połącz to samo konto i przywróć kopię. Usunięcie aplikacji z telefonu nie usuwa kopii na Drive.")
    }
    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text("Przywrócić stałe dane?") },
            text = { Text("Lokalne tagi, projekty, kategorie, ToDo, rutyny, pictogramy i osoby zostaną zastąpione kopią. Przywracanie działa tylko bez lokalnych notatek i przypisań pictogramów.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = false
                    action = "restore"
                    account = state.account
                    authorize()
                }) { Text("Przywróć") }
            },
            dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Anuluj") } },
        )
    }
    if (confirmLocal) {
        AlertDialog(
            onDismissRequest = { confirmLocal = false },
            title = { Text("Użyć lokalnej konfiguracji?") },
            text = { Text("Po reinstalacji będzie przywracana aktualna konfiguracja z tego urządzenia, zamiast danych z poprzedniej kopii. Starsza kopia pozostanie na Google. Notatki nie będą zapisane.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLocal = false
                    action = "local"
                    account = state.account
                    authorize()
                }) { Text("Zapisz nową kopię") }
            },
            dismissButton = { TextButton(onClick = { confirmLocal = false }) { Text("Anuluj") } },
        )
    }
}
