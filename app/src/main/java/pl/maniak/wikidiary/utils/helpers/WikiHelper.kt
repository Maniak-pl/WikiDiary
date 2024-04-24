package pl.maniak.wikidiary.utils.helpers

import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.utils.helpers.DateHelper.toFormattedStringWithDayName
import pl.maniak.wikidiary.utils.helpers.DateHelper.toYearString


object WikiHelper {
    private var dateMap: MutableMap<String, MutableMap<String, MutableList<String>>> = HashMap()

    fun preparingEntryOnWiki(noteList: List<WikiNote>): String {
        dateMap = HashMap()

        for (wiki in noteList) {
            addTitleWiki(wiki)
        }

        val allLinesNote = mutableListOf<String>()

        for (day in dateMap.keys) {
            allLinesNote.add(WikiParser.addHeadline(day, 2))
            allLinesNote.add("")
            for (category in dateMap[day]?.keys.orEmpty()) {
                allLinesNote.add(WikiParser.addListBold(category, 1))
                for (note in dateMap[day]?.get(category).orEmpty()) {
                    allLinesNote.add(WikiParser.addList(note, 2))
                }
                allLinesNote.add("")
            }
        }

        val wiki = StringBuilder()

        for (i in allLinesNote.indices) {
            wiki.append(allLinesNote[i]).append("\n")
        }
        return wiki.toString()
    }

    private fun addTitleWiki(wikiNote: WikiNote) {
        val data = wikiNote.date.toFormattedStringWithDayName()
        if (!dateMap.containsKey(data)) {
            dateMap[data] = HashMap()
        }
        addCategory(data, wikiNote)
    }

    private fun addCategory(date: String, wikiNote: WikiNote) {
        val category = dateMap[date]
        category?.let {
            val tag = if (wikiNote.folder.isNullOrBlank())
                wikiNote.tag
            else
                WikiParser.addProject(
                    wikiNote.tag,
                    wikiNote.folder,
                    wikiNote.date.toYearString()
                )
            if (!category.containsKey(tag)) {
                category[tag] = ArrayList()
            }
            addNote(tag, category, wikiNote.content)
        }
    }

    private fun addNote(tag: String, category: MutableMap<String, MutableList<String>>, content: String) {
        category[tag]?.add(content)
    }
}