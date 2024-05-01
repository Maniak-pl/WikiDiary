package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.screens.view.Tag
import pl.maniak.wikidiary.ui.screens.view.TagDefaults
import java.util.Locale


@Composable
fun AddScreen(
    tags: List<Tag> = emptyList(),
    onClick: (ActionClick) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val vertScrollState = rememberScrollState()
    LaunchedEffect(text) {
        vertScrollState.animateScrollTo(Int.MAX_VALUE)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TagsLayout(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            tags = tags.sortedBy { it.date },
            scrollState = vertScrollState,
            onTagClick = { tag ->
                if (text.isNotBlank()) {
                    onClick.invoke(ActionClick.AddNote(tag, text))
                    text = ""
                }
            },
            onAddTagClick = {
                if (text.isNotBlank()) {
                    onClick.invoke(ActionClick.AddTag(tag = text))
                    text = ""
                }
            },
            onEditTagClick = { tag ->
                onClick.invoke(ActionClick.EditTag(tag))
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


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsLayout(
    modifier: Modifier = Modifier,
    tags: List<Tag> = emptyList(),
    scrollState: ScrollState,
    onTagClick: (Tag) -> Unit,
    onAddTagClick: () -> Unit = {},
    onEditTagClick: (Tag) -> Unit = {},
    onClick: (ActionClick) -> Unit
) {
    FlowRow(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Bottom
    ) {
        tags.forEach { tag ->
            Tag(
                text = tag.name,
                onClick = { onTagClick(tag) },
                onLongClick = { onEditTagClick(tag) },
                colors = TagDefaults.tagColors(
                    backgroundColor = tag.color, contentColor = Color.White
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

        Tag(
            text = "\uD83D\uDCC2",
            onClick = { onClick.invoke(ActionClick.TagCreateCategory) },
        )
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