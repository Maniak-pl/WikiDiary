package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.WikiHelper
import java.util.Date

@Composable
fun PreparingNoteScreen(
    notesList: List<WikiNote> = emptyList()
) {
    var state = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(8.dp)
            .verticalScroll(state)
    ) {
        Text(text = WikiHelper.preparingEntryOnWiki(notesList))
    }
}

@Preview(showBackground = true)
@Composable
fun PreparingNoteScreenPreview() {
    PreparingNoteScreen(
        notesList = listOf(
            WikiNote(1, "ToDo", "Taking out the trash on Saturday", Date(), false),
            WikiNote(2, "Today", "I got up at 5:00", Date(), false)
        )
    )
}