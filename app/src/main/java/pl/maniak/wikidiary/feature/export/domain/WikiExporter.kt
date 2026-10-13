package pl.maniak.wikidiary.feature.export.domain

import java.util.TreeMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import pl.maniak.wikidiary.core.util.DateHelper.toFormattedStringWithDayName
import pl.maniak.wikidiary.core.wiki.WikiParser

class WikiExporter(private val contributors: List<WikiExportContributor>) {

    fun document(): Flow<String> =
        if (contributors.isEmpty()) {
            flowOf("")
        } else {
            combine(contributors.sortedBy(WikiExportContributor::order).map(WikiExportContributor::items)) { contributions ->
                render(contributions.flatMap { it })
            }
        }

    private class Day(val title: String) {
        val groups = LinkedHashMap<String, MutableList<String>>()
        val attachments = mutableListOf<String>()
    }

    private fun render(items: List<WikiExportItem>): String {
        val days = TreeMap<String, Day>()
        items.forEach { item ->
            val day = days.getOrPut(item.date.toExportDayKey()) {
                Day(item.date.toFormattedStringWithDayName())
            }
            when (item) {
                is WikiExportItem.GroupLine ->
                    day.groups.getOrPut(item.group) { mutableListOf() }.add(item.line)
                is WikiExportItem.DayAttachment -> day.attachments.add(item.line)
            }
        }

        return buildString {
            days.values.forEach { day ->
                appendLine(WikiParser.addHeadline(day.title, 2))
                appendLine()
                day.groups.forEach { (group, lines) ->
                    appendLine(WikiParser.addListBold(group, 1))
                    lines.forEach(::appendLine)
                    appendLine()
                }
                day.attachments.forEach(::appendLine)
                if (day.attachments.isNotEmpty()) {
                    appendLine()
                }
            }
        }
    }
}
