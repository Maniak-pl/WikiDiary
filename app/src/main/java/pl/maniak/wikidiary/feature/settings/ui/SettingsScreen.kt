package pl.maniak.wikidiary.feature.settings.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import pl.maniak.wikidiary.feature.export.domain.WikiPageUrl

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    SettingsScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onIntent: (SettingsIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        SectionHeader("Wygląd")
        DarkThemeItem(darkTheme = state.darkTheme, onChange = { onIntent(SettingsIntent.SetDarkTheme(it)) })
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SectionHeader("Eksport do Wiki")
        WikiPageUrlItem(
            savedUrl = state.wikiPageUrl,
            onSave = { onIntent(SettingsIntent.SaveWikiPageUrl(it)) }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun DarkThemeItem(darkTheme: Boolean?, onChange: (Boolean) -> Unit) {
    val checked = darkTheme ?: isSystemInDarkTheme()
    ListItem(
        headlineContent = { Text("Ciemny motyw") },
        supportingContent = {
            Text(
                when (darkTheme) {
                    null -> "Zgodnie z ustawieniem systemu"
                    true -> "Włączony"
                    false -> "Wyłączony"
                }
            )
        },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
    )
}

@Composable
private fun WikiPageUrlItem(savedUrl: String, onSave: (String) -> Unit) {
    var url by rememberSaveable(savedUrl) { mutableStateOf(savedUrl) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var saved by rememberSaveable { mutableStateOf(false) }
    val save = {
        val normalizedUrl = url.trim()
        if (normalizedUrl.isEmpty() || WikiPageUrl.isValid(normalizedUrl)) {
            onSave(normalizedUrl)
            saved = true
        } else {
            error = WikiPageUrl.INVALID_MESSAGE
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                error = null
                saved = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Adres strony Wiki") },
            placeholder = { Text("https://wiki.example.com/doku.php?id=...") },
            singleLine = true,
            isError = error != null,
            supportingText = {
                Text(
                    error ?: if (saved) "Zapisano." else
                        "Przycisk \uD83C\uDF10 w zakładce Eksport skopiuje notatki i otworzy ten adres."
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            if (savedUrl.isNotEmpty()) {
                TextButton(onClick = {
                    url = ""
                    onSave("")
                    saved = true
                }) { Text("Usuń adres") }
            }
            Button(onClick = save, enabled = url.trim() != savedUrl) { Text("Zapisz") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(state = SettingsUiState(wikiPageUrl = "https://wiki.example.com"), onIntent = {})
}
