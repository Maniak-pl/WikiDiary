package pl.maniak.wikidiary.feature.documents

import android.app.Activity
import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import org.koin.androidx.compose.koinViewModel
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DocumentRoute(
    onPickDate: () -> Unit,
    onClose: () -> Unit,
    viewModel: DocumentViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val status by viewModel.status.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val date by viewModel.date.collectAsState()
    var category by rememberSaveable { mutableStateOf("") }
    var tagId by rememberSaveable { mutableStateOf<Long?>(null) }
    val tag = tags.firstOrNull { it.id == tagId }
    var uri by rememberSaveable { mutableStateOf("") }
    var mimeType by rememberSaveable { mutableStateOf("application/octet-stream") }
    var originalName by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryMenu by remember { mutableStateOf(false) }
    var tagMenu by remember { mutableStateOf(false) }
    var authorizing by rememberSaveable { mutableStateOf(false) }
    var draftJson by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var showAbandon by remember { mutableStateOf(false) }
    val authorization = remember(context) { Identity.getAuthorizationClient(context) }
    val locked = status.busy || authorizing || status.pending != null
    val draft = DocumentDraft(
        uri = uri,
        mimeType = mimeType,
        extension = originalName.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" },
        subject = subject,
        description = description,
        date = date.time,
        tag = tag?.name.orEmpty(),
        documentCategory = category.takeIf { selected -> categories.any { it.name == selected } },
        noteCategory = tag?.category,
    )
    val errors = if (submitted) draft.validationErrors() else emptyMap()

    fun acceptAuthorization(result: AuthorizationResult) {
        authorizing = false
        val token = result.accessToken
        if (token.isNullOrBlank()) {
            viewModel.error("Google nie zwrócił dostępu do Drive.")
            return
        }
        viewModel.upload(token, draftJson.takeIf { it.isNotEmpty() }?.let {
            Json.decodeFromString<DocumentDraft>(it)
        })
    }

    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            authorizing = false
            viewModel.error("Anulowano dostęp do Google Drive.")
        } else {
            try {
                acceptAuthorization(authorization.getAuthorizationResultFromIntent(result.data))
            } catch (exception: Exception) {
                authorizing = false
                viewModel.error("Błąd autoryzacji Google: ${exception.message}")
            }
        }
    }

    fun authorize() {
        authorizing = true
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope("https://www.googleapis.com/auth/drive.file")))
            .setOptOutIncludingGrantedScopes(true)
        authorization.authorize(request.build())
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val intent = result.pendingIntent
                    if (intent == null) {
                        authorizing = false
                        viewModel.error("Google nie zwrócił ekranu zgody.")
                    } else consent.launch(IntentSenderRequest.Builder(intent).build())
                } else acceptAuthorization(result)
            }
            .addOnFailureListener { exception ->
                authorizing = false
                viewModel.error("Błąd autoryzacji Google: ${exception.message}")
            }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selected ->
        if (selected != null) {
            try {
                context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val name = context.contentResolver.query(selected, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                    ?: throw IllegalArgumentException("Nie można odczytać nazwy pliku.")
                val selectedMimeType = context.contentResolver.getType(selected) ?: "application/octet-stream"
                uri = selected.toString()
                mimeType = selectedMimeType
                originalName = name
            } catch (exception: Exception) {
                viewModel.error("Nie można otworzyć pliku: ${exception.message}")
            }
        }
    }

    LaunchedEffect(status.saved) {
        if (status.saved) {
            viewModel.acknowledgeSaved()
            onClose()
        }
    }
    Column(Modifier.fillMaxWidth().height(650.dp).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Dokument na Google Drive", style = MaterialTheme.typography.titleLarge)
        Text("Pola z * są wymagane. Plik trafi do głównego katalogu Mojego dysku.")
        TextButton(enabled = !locked, onClick = { filePicker.launch(arrayOf("*/*")) }) {
            Text(originalName.ifEmpty { "Wybierz plik *" })
        }
        errors[DocumentField.FILE]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        TextButton(enabled = !locked, onClick = onPickDate) {
            Text(SimpleDateFormat("yyyy.MM.dd", Locale.ROOT).format(date))
        }
        OutlinedTextField(subject, { subject = it }, label = { Text("Podmiot *") },
            isError = errors.containsKey(DocumentField.SUBJECT),
            supportingText = { Text(errors[DocumentField.SUBJECT] ?: "Np. LuxMed") },
            enabled = !locked, singleLine = true)
        OutlinedTextField(description, { description = it }, label = { Text("Opis dokumentu *") },
            isError = errors.containsKey(DocumentField.DESCRIPTION),
            supportingText = { Text(errors[DocumentField.DESCRIPTION] ?: "Np. Gastroskopia") },
            enabled = !locked, singleLine = true)
        TextButton(enabled = !locked, onClick = { categoryMenu = true }) {
            Text("Katalog dokumentu *: ${category.ifEmpty { "wybierz" }}")
        }
        DropdownMenu(categoryMenu, onDismissRequest = { categoryMenu = false }) {
            categories.forEach { item ->
                DropdownMenuItem(text = { Text(item.name) }, onClick = {
                    category = item.name
                    categoryMenu = false
                })
            }
        }
        errors[DocumentField.CATEGORY]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Katalog określa rodzaj dokumentu i grafikę. Wpis trafi do wybranego taga.")
        if (categories.isEmpty()) Text("Dodaj katalog przez komendę /kategorie.")
        TextButton(enabled = !locked, onClick = { tagMenu = true }) { Text("Tag wpisu *: ${tag?.name ?: "wybierz"}") }
        DropdownMenu(tagMenu, onDismissRequest = { tagMenu = false }) {
            tags.forEach { item ->
                DropdownMenuItem(text = { Text(item.name) }, onClick = {
                    tagId = item.id
                    item.category?.takeIf { name -> categories.any { it.name == name } }?.let { category = it }
                    tagMenu = false
                })
            }
        }
        errors[DocumentField.TAG]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (category.isNotEmpty()) Text("Grafika w Wiki: ${draft.imageFile}")
        if (subject.isNotBlank() && description.isNotBlank() && uri.isNotBlank()) {
            Text("Nazwa na Drive: ${draft.fileName}")
        }
        val pending = status.pending
        if (pending != null) {
            Text("Niedokończony zapis: ${pending.fileName}")
            Text("Ponowienie użyje tego samego pliku na Drive.")
            TextButton(enabled = !status.busy && !authorizing, onClick = { showAbandon = true }) {
                Text("Anuluj niedokończony zapis")
            }
        }
        status.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (status.busy || authorizing) CircularProgressIndicator()
        Button(enabled = !status.busy && !authorizing, onClick = {
            try {
                submitted = true
                val uploadDraft = pending ?: draft
                uploadDraft.validate()
                draftJson = Json.encodeToString(DocumentDraft.serializer(), uploadDraft)
                authorize()
            } catch (exception: IllegalArgumentException) {
                viewModel.error(exception.message ?: "Sprawdź dane dokumentu.")
            }
        }) { Text(if (pending == null) "Prześlij i zapisz" else "Ponów zapis") }
    }
    if (showAbandon) {
        AlertDialog(
            onDismissRequest = { showAbandon = false },
            title = { Text("Anulować zapis?") },
            text = {
                Text("Jeśli plik został przesłany, pozostanie na Google Drive. " +
                    "Notatka nie zostanie dodana. Po anulowaniu sprawdź plik na Drive.")
            },
            confirmButton = {
                TextButton(onClick = { showAbandon = false; viewModel.abandonPending() }) { Text("Anuluj zapis") }
            },
            dismissButton = {
                TextButton(onClick = { showAbandon = false }) { Text("Wróć") }
            },
        )
    }
}
