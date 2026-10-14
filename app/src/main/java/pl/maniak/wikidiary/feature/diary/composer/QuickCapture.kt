package pl.maniak.wikidiary.feature.diary.composer

import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.ui.DiaryNavigation

/** What the composer should do with the typed text. */
sealed interface Capture {
    data class SaveNote(val tag: Tag, val content: String, val needsCompletion: Boolean = false) : Capture
    data class AddTodo(val title: String) : Capture
    data class AddTag(val name: String) : Capture
    data class Navigate(val destination: DiaryNavigation) : Capture
    data class Rejected(val error: CaptureError) : Capture
}

enum class CaptureError(val message: String) {
    EMPTY("Wpisz treść notatki."),
    MISSING_TAG("Dodaj #tag albo wybierz tag z listy."),
    MISSING_ARGUMENT("Komenda wymaga treści, np. /todo Kupić mleko."),
    UNKNOWN_COMMAND("Nieznana komenda. Wpisz /, aby zobaczyć listę."),
}

/** Slash commands of the composer; [takesArgument] commands are completed in the text field. */
enum class ComposerCommand(
    val keyword: String,
    val icon: String,
    val description: String,
    val takesArgument: Boolean,
    private val aliases: List<String> = emptyList(),
) {
    TODO("todo", "\u2705", "Dodaj zadanie ToDo", takesArgument = true),
    FIXME("fixme", "\uD83D\uDEA7", "Notatka do uzupełnienia później (FIXME)", takesArgument = true),
    FILM("film", "\uD83C\uDFAC", "Wyszukaj film lub serial w Filmweb", takesArgument = true),
    PICTOGRAM("piktogram", "\uD83D\uDDBC\uFE0F", "Dodaj pictogram", takesArgument = false, listOf("pictogram")),
    PEOPLE("osoby", "\uD83D\uDC65", "Zarządzaj osobami", takesArgument = false, listOf("osoba")),
    TAG("tag", "\uD83C\uDFF7\uFE0F", "Utwórz tag", takesArgument = true),
    PROJECT("projekt", "\uD83C\uDFAF", "Utwórz projekt (tag z kolorem i kategorią)", takesArgument = false),
    CATEGORIES("kategorie", "\uD83D\uDCC2", "Zarządzaj kategoriami", takesArgument = false),
    DATE("data", "\uD83D\uDDD3\uFE0F", "Zmień dzień notatki", takesArgument = false);

    fun matches(keyword: String) =
        this.keyword.equals(keyword, ignoreCase = true) || aliases.any { it.equals(keyword, ignoreCase = true) }

    /** Navigation of commands that only open a panel; `null` for commands that act on text. */
    val destination: DiaryNavigation?
        get() = when (this) {
            PICTOGRAM -> DiaryNavigation.Pictograms
            PEOPLE -> DiaryNavigation.People
            PROJECT -> DiaryNavigation.CreateTag
            CATEGORIES -> DiaryNavigation.Categories
            DATE -> DiaryNavigation.PickDate
            TODO, FIXME, FILM, TAG -> null
        }
}

/** Autocomplete trigger (`@`, `#` or a leading `/`) that ends at the cursor. */
data class Token(val trigger: Char, val query: String, val start: Int)

data class TextEdit(val text: String, val cursor: Int)

/**
 * Turns composer text into an action: `/command argument`, or a note whose `#Tag` (any known tag,
 * multi-word names included) picks the tag and is removed from the content. The completion command
 * word ([FIXME_MARKER]) anywhere in a note is removed from the content and marks the note as needing
 * completion.
 */
object QuickCapture {
    private const val TRIGGERS = "@#/"
    private val FIXME_MARKER = Regex("[ \\t]*(?<=^|\\s)/fixme(?=$|[^\\p{L}\\p{N}_])", RegexOption.IGNORE_CASE)

    fun parse(text: String, tags: List<Tag>): Capture {
        val trimmed = text.trim()
        return when {
            trimmed.isEmpty() -> Capture.Rejected(CaptureError.EMPTY)
            trimmed.startsWith('/') -> parseCommand(trimmed.drop(1), tags)
            else -> parseNoteWithMarker(trimmed, tags)
        }
    }

    /** Saves [text] under the clicked [tag], honouring the completion command word like [parse] does. */
    fun parseForTag(text: String, tag: Tag): Capture {
        val trimmed = text.trim()
        return if (trimmed.isEmpty()) {
            Capture.Rejected(CaptureError.EMPTY)
        } else {
            withFixmeMarker(trimmed) { Capture.SaveNote(tag, it) }
        }
    }

    fun activeToken(text: String, cursor: Int): Token? {
        val before = text.substring(0, cursor.coerceIn(0, text.length))
        val start = before.indexOfLast { it in TRIGGERS }
        if (start < 0 || (start > 0 && !before[start - 1].isWhitespace())) return null
        val trigger = before[start]
        val query = before.substring(start + 1)
        if (query.contains('\n') || query.lastOrNull()?.isWhitespace() == true) return null
        if (trigger == '/' && (before.substring(0, start).isNotBlank() || query.any(Char::isWhitespace))) {
            return null
        }
        return Token(trigger, query, start)
    }

    fun complete(text: String, cursor: Int, token: Token, replacement: String): TextEdit {
        val end = cursor.coerceIn(token.start, text.length)
        val inserted = "$replacement "
        return TextEdit(
            text = text.substring(0, token.start) + inserted + text.substring(end),
            cursor = token.start + inserted.length
        )
    }

    private fun parseCommand(body: String, tags: List<Tag>): Capture {
        val (keyword, argument) = splitCommand(body)
        val command = ComposerCommand.entries.firstOrNull { it.matches(keyword) }
            ?: return Capture.Rejected(CaptureError.UNKNOWN_COMMAND)
        command.destination?.let { return Capture.Navigate(it) }
        return when (command) {
            ComposerCommand.FILM -> Capture.Navigate(DiaryNavigation.Filmweb(argument))
            ComposerCommand.TODO -> requireArgument(argument, Capture::AddTodo)
            ComposerCommand.FIXME -> requireArgument(argument) { markForCompletion(parseNoteWithMarker(it, tags)) }
            else -> requireArgument(argument, Capture::AddTag)
        }
    }

    private fun splitCommand(body: String): Pair<String, String> {
        val keyword = body.takeWhile { !it.isWhitespace() }
        return keyword to body.drop(keyword.length).trim()
    }

    private fun parseNoteWithMarker(text: String, tags: List<Tag>): Capture =
        withFixmeMarker(text) { parseNote(it, tags) }

    private fun withFixmeMarker(text: String, capture: (String) -> Capture): Capture {
        if (!FIXME_MARKER.containsMatchIn(text)) return capture(text)
        val content = text.replace(FIXME_MARKER, "").trim()
        return if (content.isEmpty()) {
            Capture.Rejected(CaptureError.MISSING_ARGUMENT)
        } else {
            markForCompletion(capture(content))
        }
    }

    private fun markForCompletion(capture: Capture) =
        if (capture is Capture.SaveNote) capture.copy(needsCompletion = true) else capture

    private fun requireArgument(argument: String, capture: (String) -> Capture) =
        if (argument.isEmpty()) Capture.Rejected(CaptureError.MISSING_ARGUMENT) else capture(argument)

    private fun parseNote(text: String, tags: List<Tag>): Capture {
        text.forEachIndexed { index, char ->
            if (char != '#' || (index > 0 && !text[index - 1].isWhitespace())) return@forEachIndexed
            val rest = text.substring(index + 1)
            val tag = tags
                .filter { rest.startsWith(it.name, ignoreCase = true) && endsAtWordBoundary(rest, it.name.length) }
                .maxByOrNull { it.name.length }
                ?: return@forEachIndexed
            val content = listOf(
                text.substring(0, index).trimEnd(' ', '\t'),
                text.substring(index + 1 + tag.name.length).trimStart(' ', '\t')
            ).filter(String::isNotEmpty).joinToString(" ").trim()
            return if (content.isEmpty()) Capture.Rejected(CaptureError.EMPTY) else Capture.SaveNote(tag, content)
        }
        return Capture.Rejected(CaptureError.MISSING_TAG)
    }

    private fun endsAtWordBoundary(text: String, length: Int) =
        length > 0 && (length == text.length || !text[length].isLetterOrDigit())
}
