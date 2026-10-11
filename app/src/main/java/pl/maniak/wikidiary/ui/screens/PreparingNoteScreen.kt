package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults.elevation
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.PictogramEntry
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.WikiHelper
import java.net.URI
import java.net.URISyntaxException
import java.util.Date

@Composable
fun PreparingNoteScreen(
    notesList: List<WikiNote> = emptyList(),
    filmNotes: List<FilmNoteEntry> = emptyList(),
    pictogramEntries: List<PictogramEntry> = emptyList(),
    wikiPageUrl: String = "",
    onCopyClick: (content: String) -> Unit = {},
    onSaveWikiPageUrl: (url: String) -> Unit = {},
    onOpenWikiClick: (url: String, content: String) -> Unit = { _, _ -> }
) {
    val state = rememberScrollState()
    val content = remember(notesList, filmNotes, pictogramEntries) {
        WikiHelper.preparingEntryOnWiki(notesList, filmNotes, pictogramEntries)
    }
    var showWikiUrlDialog by remember { mutableStateOf(false) }
    var wikiUrlInput by remember(wikiPageUrl) { mutableStateOf(wikiPageUrl) }
    var wikiUrlError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton =
        {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (wikiPageUrl.isNotBlank()) {
                    TextButton(
                        onClick = {
                            wikiUrlInput = wikiPageUrl
                            wikiUrlError = null
                            showWikiUrlDialog = true
                        }
                    ) {
                        Text("Zmień adres")
                    }
                }
                FloatingActionButton(
                    onClick = {
                        if (wikiPageUrl.isBlank()) {
                            wikiUrlInput = ""
                            wikiUrlError = null
                            showWikiUrlDialog = true
                        } else {
                            onOpenWikiClick(wikiPageUrl, content)
                        }
                    },
                    elevation = elevation(5.dp),
                ) {
                    if (wikiPageUrl.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Dodaj adres strony Wiki"
                        )
                    } else {
                        Text(text = "🌐", fontSize = 24.sp)
                    }
                }
                FloatingActionButton(
                    onClick = { onCopyClick(content) },
                    elevation = elevation(5.dp),
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_copy),
                        contentDescription = "Kopiuj notatki"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(state)
        ) {
            Text(text = content)
        }
    }

    if (showWikiUrlDialog) {
        AlertDialog(
            onDismissRequest = {
                showWikiUrlDialog = false
                wikiUrlError = null
            },
            title = { Text("Adres strony Wiki") },
            text = {
                Column {
                    OutlinedTextField(
                        value = wikiUrlInput,
                        onValueChange = {
                            wikiUrlInput = it
                            wikiUrlError = null
                        },
                        label = { Text("URL strony") },
                        placeholder = {
                            Text("https://wiki.example.com/doku.php?id=...")
                        },
                        isError = wikiUrlError != null,
                        singleLine = true
                    )
                    if (wikiUrlError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wikiUrlError.orEmpty(),
                            color = androidx.compose.material.MaterialTheme.colors.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val normalizedUrl = wikiUrlInput.trim()
                        if (isValidWikiUrl(normalizedUrl)) {
                            onSaveWikiPageUrl(normalizedUrl)
                            showWikiUrlDialog = false
                            wikiUrlError = null
                        } else {
                            wikiUrlError =
                                "Podaj pełny adres URL zaczynający się od http:// lub https://."
                        }
                    }
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWikiUrlDialog = false
                        wikiUrlError = null
                    }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
}

private fun isValidWikiUrl(value: String): Boolean {
    return try {
        val uri = URI(value)
        (uri.scheme.equals("http", ignoreCase = true) ||
            uri.scheme.equals("https", ignoreCase = true)) &&
            !uri.host.isNullOrBlank()
    } catch (_: URISyntaxException) {
        false
    }
}

@Preview(showBackground = true)
@Composable
fun PreparingNoteScreenPreview() {
    PreparingNoteScreen(
        notesList = listOf(
            WikiNote(1, "ToDo", "Taking out the trash on Saturday", null, Date(), false),
            WikiNote(2, "Today", "I got up at 5:00", "Work", Date(), false),
            WikiNote(3, "Regeneration", "I went to the gym", "Health:Fitness", Date(), false),
        )
    )
}