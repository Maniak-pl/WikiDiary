package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    LazyColumn {
        items(items = notesList) { note ->
            WikiNoteItem(note = note, onClick = onClick)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WikiNoteItem(
    note: WikiNote,
    onClick: (ActionClick) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .combinedClickable(
                onLongClick = {
                    onClick(ActionClick.DeleteNote(note))
                },
                onClick = { }
            ),
        elevation = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
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
    }
}

@Preview(showBackground = true)
@Composable
fun ListNotesScreenPreview() {
    ListNotesScreen(
        listOf(
            WikiNote(1, "ToDo", "Taking out the trash on Saturday", Date(), false),
            WikiNote(2, "Today", "I got up at 5:00", Date(), false)
        )
    )
}