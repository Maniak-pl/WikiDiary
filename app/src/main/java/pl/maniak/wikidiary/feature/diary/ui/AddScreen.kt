package pl.maniak.wikidiary.feature.diary.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.core.ui.component.Tag
import pl.maniak.wikidiary.core.ui.component.TagDefaults
import pl.maniak.wikidiary.core.util.DateHelper.toDayString
import java.util.Date


@Composable
fun AddScreen(
    tags: List<Tag> = emptyList(),
    people: List<Person> = emptyList(),
    selectedDate: Date,
    onClick: (ActionClick) -> Unit,
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val mentionQuery = currentMentionQuery(text)
    val personSuggestions = if (mentionQuery != null) {
        people
            .filter { it.name.contains(mentionQuery.trim(), ignoreCase = true) }
            .take(5)
    } else {
        emptyList()
    }
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
            selectedDate = selectedDate,
            scrollState = vertScrollState,
            onTagClick = { tag ->
                if (text.text.isNotBlank()) {
                    onClick.invoke(ActionClick.AddNote(tag, text.text))
                    text = TextFieldValue("")
                }
            },
            onAddTagClick = {
                if (text.text.isNotBlank()) {
                    onClick.invoke(ActionClick.AddTag(tag = text.text))
                    text = TextFieldValue("")
                }
            },
            onEditTagClick = { tag ->
                onClick.invoke(ActionClick.EditTag(tag))
            },
            onClick = onClick
        )

        Divider(
            thickness = 2.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.24f)
        )

        if (personSuggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    personSuggestions.forEach { person ->
                        Text(
                            text = "${person.gender.icon} ${person.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .clickable {
                                    text = insertPersonMention(text, person)
                                }
                        )
                    }
                }
            }
        }

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            value = text,
            onValueChange = { text = it },
            label = { Text(text = "Enter your note") },
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsLayout(
    modifier: Modifier = Modifier,
    tags: List<Tag> = emptyList(),
    selectedDate: Date,
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
                    backgroundColor = tag.color,
                    contentColor = contentColorFor(tag.color)
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
            text = "\uD83C\uDFAC Filmweb",
            onClick = { onClick.invoke(ActionClick.TagCreateFilmweb) },
        )

        Tag(
            text = "\uD83D\uDDBC\uFE0F Pictogramy",
            onClick = { onClick.invoke(ActionClick.TagCreatePictogram) },
        )

        Tag(
            text = "\uD83D\uDC65 Osoby",
            onClick = { onClick.invoke(ActionClick.TagCreatePerson) },
        )

        Tag(
            text = "\uD83D\uDCC2",
            onClick = { onClick.invoke(ActionClick.TagCreateCategory) },
        )

        Tag(
            text = "\uD83D\uDDD3\uFE0F - ${selectedDate.toDayString()}",
            onClick = { onClick.invoke(ActionClick.TagChangeDate) },
        )
    }

}

private fun currentMentionQuery(value: TextFieldValue): String? {
    val cursor = value.selection.start.coerceIn(0, value.text.length)
    val beforeCursor = value.text.substring(0, cursor)
    val atIndex = beforeCursor.lastIndexOf('@')
    if (atIndex < 0 || beforeCursor.substring(atIndex + 1).contains('\n')) {
        return null
    }
    if (atIndex > 0 && beforeCursor[atIndex - 1].isLetterOrDigit()) {
        return null
    }
    val query = beforeCursor.substring(atIndex + 1)
    return if (query.endsWith(" ") || query.endsWith("\t")) null else query
}

private fun insertPersonMention(value: TextFieldValue, person: Person): TextFieldValue {
    val cursor = value.selection.start.coerceIn(0, value.text.length)
    val beforeCursor = value.text.substring(0, cursor)
    val atIndex = beforeCursor.lastIndexOf('@')
    if (atIndex < 0) {
        return value
    }
    val mention = "@${person.name} "
    val newText = value.text.removeRange(atIndex, cursor).let {
        it.substring(0, atIndex) + mention + it.substring(atIndex)
    }
    val newCursor = atIndex + mention.length
    return TextFieldValue(newText, TextRange(newCursor))
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    AddScreen(
        tags = listOf(Tag(1, "ToDo"), Tag(2, "Today"), Tag(3, "Gym with Szymon 2024", "Health:Physical")),
        selectedDate = Date(),
        onClick = {}
    )
}