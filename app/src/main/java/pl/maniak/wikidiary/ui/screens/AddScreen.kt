package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.ui.model.ActionClick
import java.util.Date


@Composable
fun AddScreen(
    tags: List<Tag> = emptyList(),
    onClick: (ActionClick) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            TagsLayout(
                tags = tags,
                onTagClick = { tag ->
                    if (text.isNotBlank()) {
                        val wikiNote = WikiNote(
                            id = 0,
                            tag = tag.tag,
                            content = text,
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
                })
        }

        Divider(thickness = 2.dp, color = Color.Black)

        TextField(
            modifier = Modifier.fillMaxWidth().height(55.dp),
            value = text,
            onValueChange = { text = it.capitalize() },
            label = { Text(text = "Enter your note") },
        )

    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
private fun TagsLayout(
    tags: List<Tag> = emptyList(),
    onTagClick: (Tag) -> Unit,
    onAddTagClick: () -> Unit = {}
) {
    val vertScrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        FlowRow(
            modifier = Modifier.padding(4.dp)
                .align(alignment = BottomStart)
                .verticalScroll(vertScrollState),
        ) {
            tags.forEach { tag ->
                Chip(
                    modifier = Modifier.wrapContentSize().padding(2.dp),
                    onClick = { onTagClick(tag) },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color.Black,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(text = tag.tag)
                }
            }
            Chip(
                modifier = Modifier.wrapContentSize().padding(2.dp),
                onClick = { onAddTagClick() },
                border = BorderStroke(2.dp, Color.Black),
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color.Green,
                    contentColor = Color.Black
                ),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    AddScreen(
        tags = listOf(Tag(1, "ToDo"), Tag(2, "Today")),
        onClick = {}
    )
}