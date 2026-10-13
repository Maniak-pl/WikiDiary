package pl.maniak.wikidiary.feature.export.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.core.util.DateHelper.toYearString
import pl.maniak.wikidiary.core.wiki.WikiParser

/**
 * A feature's contribution to the prepared Wiki document. Register an
 * implementation in the feature's Koin module to add it to the export.
 */
fun interface WikiExportContributor {
    fun items(): Flow<List<WikiExportItem>>

    /** Lower values are rendered first within a group and a day. */
    val order: Int get() = DEFAULT_ORDER

    companion object {
        const val DEFAULT_ORDER = 100
    }
}

sealed interface WikiExportItem {
    val date: Date

    /** A line rendered under a bold group (tag or project) of its day. */
    data class GroupLine(
        override val date: Date,
        val group: String,
        val line: String
    ) : WikiExportItem

    /** A line rendered after all groups of its day. */
    data class DayAttachment(
        override val date: Date,
        val line: String
    ) : WikiExportItem
}

object WikiExportGroup {
    fun label(tag: String, category: String?, date: Date): String =
        if (category.isNullOrBlank()) {
            tag
        } else {
            WikiParser.addProject(tag, category, date.toYearString())
        }
}

fun Date.toExportDayKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this)
