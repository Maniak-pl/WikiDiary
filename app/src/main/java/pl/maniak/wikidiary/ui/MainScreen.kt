package pl.maniak.wikidiary.ui

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.feature.diary.ui.DiaryNavigation
import pl.maniak.wikidiary.feature.diary.ui.DiaryRoute
import pl.maniak.wikidiary.feature.diary.ui.NotesListRoute
import pl.maniak.wikidiary.feature.export.ui.ExportRoute
import pl.maniak.wikidiary.feature.routines.ui.RoutinesRoute
import pl.maniak.wikidiary.feature.todo.ui.TodoRoute
import pl.maniak.wikidiary.ui.Screen.*

@Composable
fun MainScreen(
    onNavigate: (DiaryNavigation) -> Unit = {},
) {
    var currentScreen by remember { mutableStateOf<Screen>(Add) }

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
                        IconButton(onClick = { currentScreen = ToDo }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
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
                is Add -> DiaryRoute(onNavigate = onNavigate)
                is ListRoutines -> RoutinesRoute()
                is ToDo -> TodoRoute()
                is PrepareNote -> ExportRoute()
                is ListNotes -> NotesListRoute()
            }
        }
    }
}

sealed class Screen(val title: String) {
    data object Add : Screen("Add Note")
    data object ListRoutines : Screen("Routines")
    data object ToDo : Screen("ToDo")
    data object PrepareNote : Screen("Prepare Note")
    data object ListNotes : Screen("List Notes")
}
