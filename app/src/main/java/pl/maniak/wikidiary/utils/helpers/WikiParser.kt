package pl.maniak.wikidiary.utils.helpers

object WikiParser {

    fun addHeadline(note: String, level: Int): String {
        val mark = when (level) {
            1 -> "======"
            2 -> "====="
            3 -> "===="
            4 -> "==="
            else -> "=="
        }
        return "$mark $note $mark"
    }

    fun addListBold(note: String, level: Int): String {
        val str = StringBuilder()
        repeat(level) {
            str.append("  ")
        }
        str.append("* **")
        str.append(note)
        str.append("**")
        return str.toString()
    }

    fun addList(note: String, level: Int): String {
        val str = StringBuilder()
        repeat(level) {
            str.append("  ")
        }
        str.append("* ")
        str.append(note)
        return str.toString()
    }

    fun addProject(tag: String, folder: String, year: String, date: String): String {
        return "${addIconCategory(folder)}[[:$folder]] - [[:$folder:Projekt - $tag ${year}]] FIXME $date"
    }

    private fun addIconCategory(category: String): String {
        val icons = mapOf(
            "Books" to "\uD83D\uDCDA ",
            "Hobby" to "\uD83D\uDD79\uFE0F ",
            "Work" to "\uD83D\uDCBC ",
            "Health" to "❤\uFE0F ",
            "Finances" to "\uD83D\uDCB0 ",
            "Community" to "\uD83D\uDC6B\uD83C\uDFFC ",
            "WikiDiary" to "\uD83D\uDCD3 ",
            "Education" to "\uD83C\uDF93 ",
            "RealEstate" to "\uD83C\uDFD8\uFE0F ",
            "Travels" to "\uD83D\uDDFA\uFE0F ",
            "Transport" to "\uD83D\uDE97 "
        )
        for ((key, icon) in icons) {
            if (category.contains(key, ignoreCase = true)) {
                return icon
            }
        }
        return ""
    }
}