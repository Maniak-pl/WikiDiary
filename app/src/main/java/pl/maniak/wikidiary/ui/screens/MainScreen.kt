package pl.maniak.wikidiary.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
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
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.theme.WikiTheme

@Composable
fun MainScreen(
    list: List<WikiNote> = mutableListOf(),
    tagList: List<Tag> = mutableListOf(),
    onClick: (ActionClick) -> Unit = {},
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Add) }
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
                        IconButton(onClick = { currentScreen = Screen.Add }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                        IconButton(onClick = { currentScreen = Screen.PrepareNote }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_code),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { currentScreen = Screen.ListNotes }) {
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
                is Screen.Add -> {
                    AddScreen(
                        tags = tagList,
                        onClick = onClick,
                    )
                }

                is Screen.PrepareNote -> {
                    PreparingNoteScreen(
                        notesList = list,
                        onCopyClick = {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("WikiNote", it))
                        }
                    )
                }

                is Screen.ListNotes -> {
                    ListNotesScreen(notesList = list, onClick = onClick)
                }
            }
        }
    }
}

sealed class Screen(val title: String) {
    data object Add : Screen("Add Note")
    data object PrepareNote : Screen("Prepare Note")
    data object ListNotes : Screen("List Notes")
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WikiTheme {
        MainScreen()
    }
}