package pl.maniak.wikidiary.feature.pictograms.domain

import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportGroup
import pl.maniak.wikidiary.feature.export.domain.WikiExportItem
import kotlinx.coroutines.flow.map
import pl.maniak.wikidiary.core.wiki.WikiParser

class PictogramExportContributor(
    private val entries: Flow<List<PictogramEntry>>
) : WikiExportContributor {

    override val order: Int = 10

    override fun items(): Flow<List<WikiExportItem>> = entries.map { entries ->
        entries.sortedBy(PictogramEntry::date).map { entry ->
            WikiExportItem.GroupLine(
                date = entry.date,
                group = WikiExportGroup.label(entry.tag, entry.category, entry.date),
                line = WikiParser.addPictogram(entry.name)
            )
        }
    }
}
