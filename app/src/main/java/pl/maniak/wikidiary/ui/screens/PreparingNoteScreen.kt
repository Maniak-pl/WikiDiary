package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults.elevation
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.WikiHelper
import java.util.Date

@Composable
fun PreparingNoteScreen(
    notesList: List<WikiNote> = emptyList(),
    onCopyClick: (content: String) -> Unit = {}
) {
    val state = rememberScrollState()
    val content by remember { mutableStateOf(WikiHelper.preparingEntryOnWiki(notesList)) }

    Scaffold(
        floatingActionButton =
        {
            FloatingActionButton(
                onClick = { onCopyClick(content) },
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_copy),

                        contentDescription = null
                    )
                },
                elevation = elevation(5.dp),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(state)
        ) {
            Text(text = content)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreparingNoteScreenPreview() {
    PreparingNoteScreen(
        notesList = listOf(
            WikiNote(1, "ToDo", "Taking out the trash on Saturday", null, Date(), false),
            WikiNote(2, "Today", "I got up at 5:00", "Work", Date(), false)
        )
    )
}