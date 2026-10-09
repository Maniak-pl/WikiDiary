package pl.maniak.wikidiary.utils.helpers

import pl.maniak.wikidiary.domain.model.FilmNoteEntry
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.DateHelper.toFormattedStringWithDayName
import pl.maniak.wikidiary.utils.helpers.DateHelper.toYearString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale

object WikiHelper {
    private data class DayNotes(
        val displayDate: String,
        val groups: LinkedHashMap<String, MutableList<String>> = LinkedHashMap(),
        val images: LinkedHashMap<String, String> = LinkedHashMap()
    )

    fun preparingEntryOnWiki(
        noteList: List<WikiNote>,
        filmNotes: List<FilmNoteEntry> = emptyList()
    ): String {
        val filmByNoteId = filmNotes.associateBy { it.noteId }
        val dayMap = LinkedHashMap<String, DayNotes>()

        noteList
            .sortedBy(WikiNote::date)
            .forEach { note ->
                val dayKey = note.date.toDayKey()
                val dayNotes = dayMap.getOrPut(dayKey) {
                    DayNotes(displayDate = note.date.toFormattedStringWithDayName())
                }
                val group = if (note.category.isNullOrBlank()) {
                    note.tag
                } else {
                    WikiParser.addProject(
                        note.tag,
                        note.category,
                        note.date.toYearString(),
                        dayNotes.displayDate
                    )
                }
                dayNotes.groups.getOrPut(group) { mutableListOf() }.add(note.content)

                filmByNoteId[note.id]?.let { filmNote ->
                    if (filmNote.metadata.canonicalUrl !in dayNotes.images) {
                        val baseFileName = FilmwebPageParser.sanitizeImageFileName(
                            filmNote.metadata.title,
                            filmNote.metadata.year
                        )
                        var fileName = baseFileName
                        var suffix = 2
                        while (fileName in dayNotes.images.values) {
                            fileName = baseFileName.removeSuffix(".jpg") + "_$suffix.jpg"
                            suffix++
                        }
                        dayNotes.images[filmNote.metadata.canonicalUrl] = fileName
                    }
                }
        }

        return buildString {
            dayMap.values.forEach { dayNotes ->
                appendLine(WikiParser.addHeadline(dayNotes.displayDate, 2))
                appendLine()
                dayNotes.groups.forEach { (group, notes) ->
                    appendLine(WikiParser.addListBold(group, 1))
                    notes.forEach { note ->
                        appendLine(WikiParser.addList(note, 2))
                    }
                    appendLine()
                }
                dayNotes.images.values.forEach { fileName ->
                    appendLine(WikiParser.addImage(fileName))
                }
                if (dayNotes.images.isNotEmpty()) {
                    appendLine()
                }
            }
        }
    }

    private fun Date.toDayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this)
    }
}