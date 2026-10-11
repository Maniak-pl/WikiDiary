package pl.maniak.wikidiary.ui.screens

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.Routine
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.PictogramEntry
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.model.ActionClick.SaveWikiPageUrl
import pl.maniak.wikidiary.ui.screens.Screen.*
import pl.maniak.wikidiary.ui.theme.WikiTheme
import java.util.Date

@Composable
fun MainScreen(
    list: List<WikiNote> = mutableListOf(),
    tagList: List<Tag> = mutableListOf(),
    selectedDate: Date,
    routines: List<Routine> = emptyList(),
    filmNotes: List<FilmNoteEntry> = emptyList(),
    pictogramEntries: List<PictogramEntry> = emptyList(),
    wikiPageUrl: String = "",
    onClick: (ActionClick) -> Unit = {},
) {
    var currentScreen by remember { mutableStateOf<Screen>(Add) }
    val context = LocalContext.current
    val clipboardManager =
        LocalContext.current.getSystemService(ComponentActivity.CLIPBOARD_SERVICE) as ClipboardManager

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = currentScreen.title)
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        IconButton(onClick = { currentScreen = Add }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                        IconButton(onClick = { currentScreen = ListRoutines }) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                        IconButton(onClick = { currentScreen = PrepareNote }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_code),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { currentScreen = ListNotes }) {
                            Icon(Icons.Default.List, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            when (currentScreen) {
                is Add -> {
                    AddScreen(
                        tags = tagList,
                        selectedDate = selectedDate,
                        onClick = onClick,
                    )
                }

                is ListRoutines -> {
                    RoutineScreen(routines = routines, onClick = onClick)
                }

                is PrepareNote -> {
                    PreparingNoteScreen(
                        notesList = list,
                        filmNotes = filmNotes,
                        pictogramEntries = pictogramEntries,
                        wikiPageUrl = wikiPageUrl,
                        onCopyClick = {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("WikiNote", it))
                        },
                        onSaveWikiPageUrl = {
                            onClick(SaveWikiPageUrl(it))
                        },
                        onOpenWikiClick = { url, content ->
                            clipboardManager.setPrimaryClip(
                                ClipData.newPlainText("WikiNote", content)
                            )
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                )
                                Toast.makeText(
                                    context,
                                    "Notatki skopiowano do schowka. Wklej je na stronie Wiki.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "Nie znaleziono aplikacji do otwarcia strony Wiki.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }

                is ListNotes -> {
                    ListNotesScreen(
                        notesList = list,
                        pictogramEntries = pictogramEntries,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

sealed class Screen(val title: String) {
    data object Add : Screen("Add Note")
    data object ListRoutines : Screen("Routines")
    data object PrepareNote : Screen("Prepare Note")
    data object ListNotes : Screen("List Notes")
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WikiTheme {
        MainScreen(selectedDate = Date())
    }
}