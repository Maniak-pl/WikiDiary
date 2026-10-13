package pl.maniak.wikidiary.feature.diary.domain

import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportGroup
import pl.maniak.wikidiary.feature.export.domain.WikiExportItem
import kotlinx.coroutines.flow.map
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.people.domain.PersonMentionResolver

class NotesExportContributor(
    private val notes: Flow<List<WikiNote>>
) : WikiExportContributor {

    override val order: Int = 0

    override fun items(): Flow<List<WikiExportItem>> = notes.map { notes ->
        notes.sortedBy(WikiNote::date).map { note ->
            WikiExportItem.GroupLine(
                date = note.date,
                group = WikiExportGroup.label(note.tag, note.category, note.date),
                line = WikiParser.addList(
                    PersonMentionResolver.replaceWithWikiLinks(note.content, note.people),
                    2
                )
            )
        }
    }
}
