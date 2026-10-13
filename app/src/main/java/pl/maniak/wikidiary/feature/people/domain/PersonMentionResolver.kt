package pl.maniak.wikidiary.feature.people.domain

import pl.maniak.wikidiary.core.wiki.WikiParser


object PersonMentionResolver {
    fun resolve(content: String, people: List<Person>): List<Person> {
        return people
            .mapNotNull { person ->
                val mention = mentionRegex(person).find(content)
                mention?.let { it.range.first to person }
            }
            .sortedBy { it.first }
            .map { it.second }
            .distinctBy { if (it.id != 0L) it.id else it.name.lowercase() }
    }

    fun replaceWithWikiLinks(content: String, people: List<Person>): String {
        return people
            .sortedByDescending { it.name.length }
            .fold(content) { currentContent, person ->
                mentionRegex(person).replace(currentContent) {
                    WikiParser.addPerson(person)
                }
            }
    }

    private fun mentionRegex(person: Person): Regex {
        return Regex(
            "(?i)(?<![\\p{L}\\p{N}_])@" +
                Regex.escape(person.name) +
                "(?![\\p{L}\\p{N}_])"
        )
    }
}
