package pl.maniak.wikidiary.ui.screens

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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.WikiNote
import pl.maniak.wikidiary.ui.theme.WikiTheme
import java.util.Date

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Add) }
    val list = listOf(
        WikiNote(1, "ToDo", "Taking out the trash on Saturday", Date(), false),
        WikiNote(2, "Today", "I got up at 5:00", Date(), false)
    )

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
                    AddScreen()
                }

                is Screen.PrepareNote -> {
                    PreparingNoteScreen(notesList = list)
                }

                is Screen.ListNotes -> {
                    ListNotesScreen(notesList = list)
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