package pl.maniak.wikidiary.feature.diary.ui

import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.remember
import pl.maniak.wikidiary.feature.diary.composer.Capture
import pl.maniak.wikidiary.feature.diary.composer.CaptureError
import pl.maniak.wikidiary.feature.diary.composer.ComposerCommand
import pl.maniak.wikidiary.feature.diary.composer.QuickCapture
import pl.maniak.wikidiary.feature.diary.composer.Token
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.core.ui.component.Tag
import pl.maniak.wikidiary.core.ui.component.TagDefaults
import pl.maniak.wikidiary.core.util.DateHelper.toDayString
import java.util.Date


@Composable
fun DiaryRoute(
    onNavigate: (DiaryNavigation) -> Unit,
    viewModel: DiaryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    AddScreen(
        tags = state.tags,
        people = state.people,
        selectedDate = state.selectedDate,
        onIntent = viewModel::onIntent,
        onNavigate = onNavigate
    )
}

@Composable
fun AddScreen(
    tags: List<Tag> = emptyList(),
    people: List<Person> = emptyList(),
    selectedDate: Date,
    onIntent: (DiaryIntent) -> Unit,
    onNavigate: (DiaryNavigation) -> Unit,
) {
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var error by rememberSaveable { mutableStateOf<CaptureError?>(null) }
    val token = QuickCapture.activeToken(text.text, text.selection.start)
    val suggestions = suggestionsFor(token, tags, people)
    val vertScrollState = rememberScrollState()
    LaunchedEffect(tags.size) {
        vertScrollState.animateScrollTo(Int.MAX_VALUE)
    }

    fun clear() {
        text = TextFieldValue("")
        error = null
    }

    fun submit(capture: Capture) {
        when (capture) {
            is Capture.SaveNote -> onIntent(DiaryIntent.AddNote(capture.tag, capture.content, capture.needsCompletion))
            is Capture.AddTodo -> onIntent(DiaryIntent.AddTodo(capture.title))
            is Capture.AddTag -> onIntent(DiaryIntent.AddTag(name = capture.name))
            is Capture.Navigate -> onNavigate(capture.destination)
            is Capture.Rejected -> {
                error = capture.error
                return
            }
        }
        clear()
    }

    fun runCommand(command: ComposerCommand) {
        val destination = command.destination
        if (destination != null) {
            onNavigate(destination)
            clear()
        } else {
            val prefix = "/${command.keyword} "
            text = TextFieldValue(prefix, TextRange(prefix.length))
            error = null
        }
    }

    fun pick(suggestion: Suggestion) {
        val active = token ?: return
        if (suggestion is Suggestion.Command && suggestion.command.destination != null) {
            runCommand(suggestion.command)
            return
        }
        val edit = QuickCapture.complete(text.text, text.selection.start, active, suggestion.replacement)
        text = TextFieldValue(edit.text, TextRange(edit.cursor))
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
                    submit(QuickCapture.parseForTag(text.text, tag))
                }
            },
            onEditTagClick = { tag ->
                onNavigate(DiaryNavigation.EditTag(tag))
            },
            onNavigate = onNavigate
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (suggestions.isNotEmpty()) {
            SuggestionList(suggestions = suggestions, onPick = ::pick)
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = {
                if (it.text != text.text) error = null
                text = it
            },
            placeholder = { Text(text = "Notatka… #tag @osoba /komenda") },
            leadingIcon = { CommandMenu(onCommand = ::runCommand) },
            trailingIcon = {
                IconButton(
                    onClick = { submit(QuickCapture.parse(text.text, tags)) },
                    enabled = text.text.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Wyślij")
                }
            },
            isError = error != null,
            supportingText = error?.let { { Text(it.message) } },
        )
    }
}

private sealed interface Suggestion {
    val label: String
    val replacement: String

    data class Person(val person: pl.maniak.wikidiary.feature.people.domain.Person) : Suggestion {
        override val label = "${person.gender.icon} ${person.name}"
        override val replacement = "@${person.name}"
    }

    data class TagName(val name: String) : Suggestion {
        override val label = "#$name"
        override val replacement = "#$name"
    }

    data class Command(val command: ComposerCommand) : Suggestion {
        override val label = "${command.icon} /${command.keyword} — ${command.description}"
        override val replacement = "/${command.keyword}"
    }
}

private const val MAX_SUGGESTIONS = 5

private fun suggestionsFor(token: Token?, tags: List<Tag>, people: List<Person>): List<Suggestion> =
    when (token?.trigger) {
        '@' -> people
            .filter { it.name.contains(token.query, ignoreCase = true) }
            .take(MAX_SUGGESTIONS)
            .map(Suggestion::Person)
        '#' -> tags
            .map { it.name }
            .distinct()
            .filter { it.contains(token.query, ignoreCase = true) }
            .take(MAX_SUGGESTIONS)
            .map(Suggestion::TagName)
        '/' -> ComposerCommand.entries
            .filter { it.keyword.startsWith(token.query, ignoreCase = true) }
            .map(Suggestion::Command)
        else -> emptyList()
    }

@Composable
private fun SuggestionList(suggestions: List<Suggestion>, onPick: (Suggestion) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            suggestions.forEach { suggestion ->
                Text(
                    text = suggestion.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun CommandMenu(onCommand: (ComposerCommand) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.AddCircle, contentDescription = "Komendy")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ComposerCommand.entries.forEach { command ->
                DropdownMenuItem(
                    text = { Text(command.description) },
                    leadingIcon = { Text(command.icon) },
                    trailingIcon = { Text("/${command.keyword}", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {
                        expanded = false
                        onCommand(command)
                    }
                )
            }
        }
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
    onEditTagClick: (Tag) -> Unit = {},
    onNavigate: (DiaryNavigation) -> Unit
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
                colors = TagDefaults.filledTagColors(tag.color),
            )
        }

        Tag(
            text = "\uD83D\uDDD3\uFE0F - ${selectedDate.toDayString()}",
            onClick = { onNavigate(DiaryNavigation.PickDate) },
        )
    }

}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    AddScreen(
        tags = listOf(Tag(1, "ToDo"), Tag(2, "Today"), Tag(3, "Gym with Szymon 2024", "Health:Physical")),
        selectedDate = Date(),
        onIntent = {},
        onNavigate = {}
    )
}