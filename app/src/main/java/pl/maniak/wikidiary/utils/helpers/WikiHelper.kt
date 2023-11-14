package pl.maniak.wikidiary.utils.helpers

import pl.maniak.wikidiary.data.WikiNote

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
        val data = DateHelper.parseDateToStringWithDayName(wikiNote.date)
        if (!dateMap.containsKey(data)) {
            dateMap[data] = HashMap()
        }
        addCategory(data, wikiNote)
    }

    private fun addCategory(date: String, wikiNote: WikiNote) {
        val category = dateMap[date]
        category?.let {
            if (!category.containsKey(wikiNote.tag)) {
                category[wikiNote.tag] = ArrayList()
            }
            addNote(category, wikiNote)
        }
    }

    private fun addNote(category: MutableMap<String, MutableList<String>>, wikiNote: WikiNote) {
        category[wikiNote.tag]?.add(wikiNote.content)
    }
}