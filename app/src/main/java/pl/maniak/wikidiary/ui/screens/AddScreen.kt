package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.screens.tag.Tag
import pl.maniak.wikidiary.ui.screens.tag.TagDefaults
import java.util.Date
import java.util.Locale


@Composable
fun AddScreen(
    tags: List<Tag> = emptyList(),
    onClick: (ActionClick) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        TagsLayout(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            tags = tags,
            onTagClick = { tag ->
                if (text.isNotBlank()) {
                    val wikiNote = WikiNote(
                        id = 0,
                        tag = tag.tag,
                        content = text,
                        folder = tag.folder,
                        date = Date(),
                        isSend = false
                    )
                    onClick.invoke(ActionClick.AddNote(wikiNote))
                    text = ""
                }
            },
            onAddTagClick = {
                if (text.isNotBlank()) {
                    onClick.invoke(ActionClick.AddTag(Tag(id = 0, tag = text)))
                    text = ""
                }
            },
            onRemoveTagClick = { tag ->
                onClick.invoke(ActionClick.DeleteTag(tag))
            },
            onClick = onClick
        )

        Divider(thickness = 2.dp, color = Color.Black)

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            value = text,
            onValueChange = { text = it.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) } },
            label = { Text(text = "Enter your note") },
        )
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
private fun TagsLayout(
    modifier: Modifier = Modifier,
    tags: List<Tag> = emptyList(),
    onTagClick: (Tag) -> Unit,
    onAddTagClick: () -> Unit = {},
    onRemoveTagClick: (Tag) -> Unit = {},
    onClick: (ActionClick) -> Unit
) {
    val vertScrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        FlowRow(
            modifier = Modifier
                .padding(4.dp)
                .align(alignment = BottomStart)
                .verticalScroll(vertScrollState),
        ) {
            tags.forEach { tag ->
                Tag(
                    text = tag.tag,
                    onClick = { onTagClick(tag) },
                    onLongClick = { onRemoveTagClick(tag) },
                    colors = TagDefaults.tagColors(
                        backgroundColor = if (tag.folder == null) Color.Black else Color.Red,
                        contentColor = Color.White
                    ),
                )
            }

            Tag(
                text = stringResource(R.string.label_tag),
                onClick = { onAddTagClick() },
            )

            Tag(
                text = stringResource(id = R.string.label_project),
                onClick = { onClick.invoke(ActionClick.TagCreateProject) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    AddScreen(
        tags = listOf(Tag(1, "ToDo"), Tag(2, "Today"), Tag(3, "Gym with Szymon 2024", "Health:Physical")),
        onClick = {}
    )
}