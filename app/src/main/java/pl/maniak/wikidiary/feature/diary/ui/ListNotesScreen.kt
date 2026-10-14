package pl.maniak.wikidiary.feature.diary.ui

import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.core.util.formatDateString
import java.util.Date

@Composable
fun NotesListRoute(viewModel: NotesListViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    ListNotesScreen(
        notesList = state.notes,
        pictogramEntries = state.pictogramEntries,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun ListNotesScreen(
    notesList: List<WikiNote> = emptyList(),
    pictogramEntries: List<PictogramEntry> = emptyList(),
    onIntent: (NotesListIntent) -> Unit = {},
) {
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedNotes by remember { mutableStateOf(setOf<Long>()) }
    var isCompletionFilterOn by rememberSaveable { mutableStateOf(false) }
    val notesToCompleteCount = notesList.count(WikiNote::needsCompletion)
    val showOnlyNotesToComplete = isCompletionFilterOn && notesToCompleteCount > 0
    val visibleNotes = if (showOnlyNotesToComplete) notesList.filter(WikiNote::needsCompletion) else notesList
    val pictogramsByGroup =
        if (showOnlyNotesToComplete) emptyMap() else pictogramEntries.groupBy { it.groupKey() }
    val lastNoteByGroup = visibleNotes
        .groupBy { it.groupKey() }
        .mapValues { (_, notes) -> notes.last().id }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedNotes = emptySet()
    }

    Column {
        if (isSelectionMode) {
            SelectionToolbar(
                selectedCount = selectedNotes.size,
                totalCount = visibleNotes.size,
                onCancel = ::exitSelectionMode,
                onDelete = {
                    onIntent(NotesListIntent.DeleteNotes(selectedNotes.toList()))
                    exitSelectionMode()
                },
                onToggleNeedsCompletion = {
                    val markAll = !visibleNotes
                        .filter { it.id in selectedNotes }
                        .all(WikiNote::needsCompletion)
                    onIntent(NotesListIntent.SetNeedsCompletion(selectedNotes.toList(), markAll))
                    exitSelectionMode()
                },
                onSelectAllToggle = {
                    selectedNotes =
                        if (selectedNotes.size == visibleNotes.size) emptySet() else visibleNotes.map { it.id }
                            .toSet()
                }
            )
        } else if (notesToCompleteCount > 0) {
            FilterChip(
                modifier = Modifier.padding(horizontal = 10.dp),
                selected = showOnlyNotesToComplete,
                onClick = { isCompletionFilterOn = !isCompletionFilterOn },
                label = { Text("\uD83D\uDEA7 Do uzupełnienia ($notesToCompleteCount)") }
            )
        }

        LazyColumn {
            visibleNotes.forEach { note ->
                item(key = "note_${note.id}") {
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
                            }
                        },
                        onLongClick = {
                            isSelectionMode = true
                            selectedNotes = setOf(note.id)
                        }
                    )
                }

                val groupKey = note.groupKey()
                val entries = pictogramsByGroup[groupKey]
                if (lastNoteByGroup[groupKey] == note.id && !entries.isNullOrEmpty()) {
                    item(key = "pictograms_${groupKey.tag}_${groupKey.day}") {
                        PictogramEntriesItem(
                            tag = groupKey.tag,
                            date = note.date,
                            entries = entries,
                            onIntent = onIntent
                        )
                    }
                }
            }

            pictogramsByGroup
                .filterKeys { it !in lastNoteByGroup.keys }
                .forEach { (groupKey, entries) ->
                    item(key = "pictograms_${groupKey.tag}_${groupKey.day}") {
                        PictogramEntriesItem(
                            tag = groupKey.tag,
                            date = entries.first().date,
                            entries = entries,
                            onIntent = onIntent
                        )
                    }
                }
        }
    }
}

@Composable
private fun PictogramEntriesItem(
    tag: String,
    date: Date,
    entries: List<PictogramEntry>,
    onIntent: (NotesListIntent) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$tag - pictogramy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = formatDateString(date),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = entry.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.name,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp
                    )
                    IconButton(
                        onClick = {
                            onIntent(NotesListIntent.DeletePictogramEntry(entry.id))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń pictogram"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionToolbar(
    selectedCount: Int,
    totalCount: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onToggleNeedsCompletion: () -> Unit,
    onSelectAllToggle: () -> Unit
) {
    val toolbarBackground = MaterialTheme.colorScheme.primaryContainer
    val toolbarContent = MaterialTheme.colorScheme.onPrimaryContainer

    TopAppBar(
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = toolbarBackground,
            titleContentColor = toolbarContent,
            actionIconContentColor = toolbarContent
        ),
        title = { Text("Zaznaczono: $selectedCount / $totalCount") },
        actions = {
            Checkbox(
                checked = selectedCount == totalCount && totalCount > 0,
                onCheckedChange = { onSelectAllToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = toolbarContent,
                    uncheckedColor = toolbarContent,
                    checkmarkColor = toolbarBackground
                )
            )

            IconButton(onClick = onToggleNeedsCompletion) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Oznacz lub odznacz do uzupełnienia",
                    tint = toolbarContent
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = toolbarContent
                )
            }

            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Anuluj",
                    tint = toolbarContent
                )
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .scale(scale),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(!isSelected) },
                    onLongClick = onLongClick
                )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.tag,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        if (note.needsCompletion) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FIXME",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

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
                    style = MaterialTheme.typography.bodyLarge
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
            WikiNote(2, "Today", "I got up at 5:00", "", Date(), false, needsCompletion = true)
        )
    )
}

private data class NoteGroupKey(
    val tag: String,
    val day: String
)

private fun WikiNote.groupKey(): NoteGroupKey =
    NoteGroupKey(tag = tag, day = date.toDayKey())

private fun PictogramEntry.groupKey(): NoteGroupKey =
    NoteGroupKey(tag = tag, day = date.toDayKey())

private fun Date.toDayKey(): String =
    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(this)
