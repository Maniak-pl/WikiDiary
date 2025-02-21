package pl.maniak.wikidiary.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.ui.model.ActionClick
import java.util.Date

@Composable
fun ListNotesScreen(
    notesList: List<WikiNote> = emptyList(),
    onClick: (ActionClick) -> Unit = {},
) {
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedNotes by remember { mutableStateOf(setOf<Long>()) }

    Column {
        if (isSelectionMode) {
            SelectionToolbar(
                selectedCount = selectedNotes.size,
                totalCount = notesList.size,
                onCancel = {
                    isSelectionMode = false
                    selectedNotes = emptySet()
                },
                onDelete = {
                    onClick(ActionClick.DeleteNotes(selectedNotes.toList()))
                    isSelectionMode = false
                    selectedNotes = emptySet()
                },
                onSelectAllToggle = {
                    selectedNotes =
                        if (selectedNotes.size == notesList.size) emptySet() else notesList.map { it.id }
                            .toSet()
                }
            )
        }

        LazyColumn {
            items(items = notesList) { note ->
                WikiNoteItem(
                    note = note,
                    isSelected = selectedNotes.contains(note.id),
                    isSelectionMode = isSelectionMode,
                    onClick = { selected ->
                        if (isSelectionMode) {
                            selectedNotes = if (selected) {
                                selectedNotes + note.id
                            } else {
                                selectedNotes - note.id
                            }
                        } else {
                            onClick(ActionClick.EditNote(note))
                        }
                    },
                    onLongClick = {
                        isSelectionMode = true
                        selectedNotes = setOf(note.id)
                    }
                )
            }
        }
    }
}

@Composable
fun SelectionToolbar(
    selectedCount: Int,
    totalCount: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSelectAllToggle: () -> Unit
) {
    TopAppBar(
        title = { Text("Zaznaczono: $selectedCount / $totalCount") },
        actions = {
            Checkbox(
                checked = selectedCount == totalCount && totalCount > 0,
                onCheckedChange = { onSelectAllToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.White,
                    checkmarkColor = Color.Black
                )
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.White)
            }

            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Anuluj", tint = Color.White)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WikiNoteItem(
    note: WikiNote,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: (Boolean) -> Unit,
    onLongClick: () -> Unit,
) {
    val scale = animateFloatAsState(if (isSelected) 0.95f else 1f).value

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .scale(scale)
            .combinedClickable(
                onClick = { onClick(!isSelected) },
                onLongClick = onLongClick
            ),
        elevation = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = note.tag,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Text(
                        text = note.formatDateString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = note.content,
                    modifier = Modifier.padding(4.dp),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body1
                )
            }
            if (isSelectionMode) {
                Spacer(modifier = Modifier.width(4.dp))
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick(it) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListNotesScreenPreview() {
    ListNotesScreen(
        listOf(
            WikiNote(1, "ToDo", "Taking out the trash on Saturday", "", Date(), false),
            WikiNote(2, "Today", "I got up at 5:00", "", Date(), false)
        )
    )
}
