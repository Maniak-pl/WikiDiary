package pl.maniak.wikidiary.feature.filmweb.domain

import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportItem
import pl.maniak.wikidiary.feature.export.domain.toExportDayKey
import kotlinx.coroutines.flow.combine
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import pl.maniak.wikidiary.feature.filmweb.data.FilmwebPageParser

/** Adds one poster per film and day, giving colliding file names a numeric suffix. */
class FilmPosterExportContributor(
    private val notes: Flow<List<WikiNote>>,
    private val filmNotes: Flow<List<FilmNoteEntry>>
) : WikiExportContributor {

    override val order: Int = 20

    override fun items(): Flow<List<WikiExportItem>> =
        combine(notes, filmNotes) { notes, filmNotes ->
            val filmByNoteId = filmNotes.associateBy(FilmNoteEntry::noteId)
            val fileNamesByDay = HashMap<String, LinkedHashMap<String, String>>()
            notes.sortedBy(WikiNote::date).mapNotNull { note ->
                val metadata = filmByNoteId[note.id]?.metadata ?: return@mapNotNull null
                val dayImages = fileNamesByDay.getOrPut(note.date.toExportDayKey()) { LinkedHashMap() }
                if (metadata.canonicalUrl in dayImages) return@mapNotNull null

                val baseFileName = FilmwebPageParser.sanitizeImageFileName(metadata.title, metadata.year)
                var fileName = baseFileName
                var suffix = 2
                while (fileName in dayImages.values) {
                    fileName = baseFileName.removeSuffix(".jpg") + "_$suffix.jpg"
                    suffix++
                }
                dayImages[metadata.canonicalUrl] = fileName
                WikiExportItem.DayAttachment(date = note.date, line = WikiParser.addImage(fileName))
            }
        }
}
