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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
            onProjectClick = { name, category ->
                onClick.invoke(ActionClick.AddTag(Tag(id = 0, tag = name, folder = category)))
            },
            onRemoveTagClick = { tag ->
                onClick.invoke(ActionClick.DeleteTag(tag))
            }
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
    onProjectClick: (name: String, category: String) -> Unit = { _, _ -> },
    onRemoveTagClick: (Tag) -> Unit = {}
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
                    onClick = { onTagClick(tag) },
                    onLongClick = { onRemoveTagClick(tag) },
                    colors = TagDefaults.tagColors(
                        backgroundColor = if (tag.folder == null) Color.Black else Color.Red,
                        contentColor = Color.White
                    ),
                    folderName = tag.folder
                ) {
                    Text(text = tag.tag)
                }
            }
            Tag(
                onClick = { onAddTagClick() },
                colors = TagDefaults.tagColors(
                    backgroundColor = Color.Green,
                    contentColor = Color.Black
                ),
            ) {
                Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_add), contentDescription = null)
            }
            CreateProjectDialog(onProjectClick = onProjectClick)
        }
    }
}

@Composable
fun CreateProjectDialog(
    onProjectClick: (name: String, category: String) -> Unit = { _, _ -> }
) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Tag(
        onClick = { showDialog = true },
        colors = TagDefaults.tagColors(
            backgroundColor = Color.Red,
            contentColor = Color.Black
        ),
    ) {
        Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_project), contentDescription = null)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(id = R.string.action_add_project)) },
            text = {
                Column {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.label_name)) }
                    )
                    TextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(stringResource(id = R.string.label_category)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && category.isNotBlank()) {
                            onProjectClick(name, category)
                            name = ""
                            category = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
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