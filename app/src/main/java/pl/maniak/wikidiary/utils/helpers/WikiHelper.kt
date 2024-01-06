package pl.maniak.wikidiary.utils.helpers

import pl.maniak.wikidiary.domain.model.WikiNote

object WikiHelper {
    private var dateMap: MutableMap<String, MutableMap<String, MutableMap<String?, MutableList<String>>>> = HashMap()

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
                for (folder in dateMap[day]?.get(category)?.keys.orEmpty()) {
                    folder?.let {
                        val folderPrefix = WikiParser.addPage(it)
                        allLinesNote.add(WikiParser.addListBold(folderPrefix, 2))
                        for (note in dateMap[day]?.get(category)?.get(folder).orEmpty()) {
                            allLinesNote.add(WikiParser.addList(note, 3))
                        }
                    } ?: run {
                        for (note in dateMap[day]?.get(category)?.get(null).orEmpty()) {
                            allLinesNote.add(WikiParser.addList(note, 2))
                        }
                    }
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
                category[wikiNote.tag] = HashMap()
            }
            addFolder(date, wikiNote)
        }
    }

    private fun addFolder(date: String, wikiNote: WikiNote) {
        val category = dateMap[date]?.get(wikiNote.tag)
        category?.let {
            val folder = wikiNote.folder
            if (!category.containsKey(folder)) {
                category[folder] = ArrayList()
            }
            addNote(category, wikiNote)
        }
    }

    private fun addNote(folder: MutableMap<String?, MutableList<String>>, wikiNote: WikiNote) {
        folder.computeIfAbsent(wikiNote.folder) { ArrayList() }.add(wikiNote.content)
    }
}