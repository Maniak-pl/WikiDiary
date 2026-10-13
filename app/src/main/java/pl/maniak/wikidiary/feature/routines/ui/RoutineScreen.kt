package pl.maniak.wikidiary.feature.routines.ui

import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.feature.routines.domain.Routine

@Composable
fun RoutinesRoute(viewModel: RoutinesViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    RoutineScreen(routines = state.routines, onIntent = viewModel::onIntent)
}

@Composable
fun RoutineScreen(routines: List<Routine> = emptyList(), onIntent: (RoutinesIntent) -> Unit) {
    var newRoutineName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newRoutineName,
                onValueChange = { newRoutineName = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                label = { Text("New routine") }
            )
            IconButton(
                onClick = {
                    if (newRoutineName.isNotEmpty()) {
                        onIntent(RoutinesIntent.Add(newRoutineName))
                        newRoutineName = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add a routine")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(routines) { routine ->
                RoutineItem(
                    routine,
                    onRemove = { onIntent(RoutinesIntent.Delete(routine.id)) },
                    onToggle = { isChecked ->
                        onIntent(RoutinesIntent.SetCompleted(routine.id, isChecked))
                    }
                )
            }
        }
    }
}

@Composable
fun RoutineItem(routine: Routine, onRemove: () -> Unit, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = routine.isCompleted,
            onCheckedChange = { onToggle(it) }
        )
        Text(
            routine.name, modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove the routine")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    RoutineScreen(
        listOf(
            Routine(1, "Yoga"),
            Routine(2, "Shopping"),
            Routine(3, "Book"),
        )
    ) { }
}
