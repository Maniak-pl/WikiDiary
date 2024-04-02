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

    fun addProject(tag: String, folder: String): String {
        return "[[:$folder]] - [[:$folder:Projekt - $tag]]"
    }
}