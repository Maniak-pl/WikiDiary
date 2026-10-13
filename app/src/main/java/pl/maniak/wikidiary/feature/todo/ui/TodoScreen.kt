package pl.maniak.wikidiary.feature.todo.ui

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
import pl.maniak.wikidiary.feature.todo.domain.Todo

@Composable
fun TodoRoute(viewModel: TodoViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    TodoScreen(todos = state.todos, onIntent = viewModel::onIntent)
}

@Composable
fun TodoScreen(
    todos: List<Todo> = emptyList(),
    onIntent: (TodoIntent) -> Unit
) {
    var newTodoTitle by remember { mutableStateOf("") }

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
                value = newTodoTitle,
                onValueChange = { newTodoTitle = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                label = { Text("New task") }
            )
            IconButton(
                onClick = {
                    if (newTodoTitle.isNotBlank()) {
                        onIntent(TodoIntent.Add(newTodoTitle.trim()))
                        newTodoTitle = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add a task")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(todos, key = { it.id }) { todo ->
                TodoItem(
                    todo = todo,
                    onRemove = { onIntent(TodoIntent.Delete(todo.id)) },
                    onToggle = { isChecked ->
                        onIntent(TodoIntent.SetCompleted(todo.id, isChecked))
                    }
                )
            }
        }
    }
}

@Composable
private fun TodoItem(
    todo: Todo,
    onRemove: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = onToggle
        )
        Text(
            text = todo.title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove the task")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    TodoScreen(
        todos = listOf(
            Todo(id = 1, title = "Prepare release"),
            Todo(id = 2, title = "Review notes", isCompleted = true)
        ),
        onIntent = {}
    )
}
