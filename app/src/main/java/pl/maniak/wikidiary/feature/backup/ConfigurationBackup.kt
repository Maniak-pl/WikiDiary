package pl.maniak.wikidiary.feature.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import java.security.MessageDigest

@Serializable
data class ConfigurationBackup(
    val version: Int = 1,
    val tags: List<BackupTag>,
    val categories: List<BackupCategory>,
    val todos: List<BackupItem>,
    val routines: List<BackupItem>,
    val pictograms: List<BackupPictogram>,
    val people: List<BackupPerson>,
) {
    fun validate() {
        require(version == 1) { "Ta wersja kopii wymaga aktualizacji aplikacji." }
        fun validItems(items: List<Pair<Long, String>>) {
            require(items.all { it.first > 0 && it.second.isNotBlank() } &&
                items.map { it.first }.distinct().size == items.size
            ) { "Kopia zawiera niepoprawne lub powtórzone elementy." }
        }
        validItems(tags.map { it.id to it.name })
        validItems(categories.map { it.id to it.name })
        validItems(todos.map { it.id to it.name })
        validItems(routines.map { it.id to it.name })
        validItems(pictograms.map { it.id to it.name })
        validItems(people.map { it.id to it.name })
        require(categories.map { it.name }.distinct().size == categories.size) { "Powtórzone kategorie w kopii." }
        require(categories.all { category ->
            category.parentId == null || categories.any { it.id == category.parentId && it.id != category.id }
        }) { "Brak nadrzędnej kategorii w kopii." }
        require(pictograms.map { WikiParser.pictogramFileName(it.name) }.distinct().size == pictograms.size &&
            pictograms.all { it.icon.isNotBlank() }
        ) { "Niepoprawne pictogramy w kopii." }
        require(people.all { person -> PersonGender.entries.any { it.storageValue == person.gender } }) {
            "Niepoprawna ikona osoby w kopii."
        }
    }

    fun hash(): String = MessageDigest.getInstance("SHA-256")
        .digest(Json.encodeToString(serializer(), this).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

@Serializable
data class BackupFile(val savedAt: Long, val data: ConfigurationBackup) {
    fun encode(): String = Json.encodeToString(serializer(), this)

    companion object {
        const val MAX_BYTES = 5 * 1024 * 1024

        fun decode(content: String): BackupFile {
            require(content.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "Kopia jest zbyt duża." }
            return Json.decodeFromString<BackupFile>(content).also {
                require(it.savedAt > 0) { "Niepoprawna data kopii." }
                it.data.validate()
            }
        }
    }
}

@Serializable
data class BackupTag(val id: Long, val name: String, val category: String?, val date: Long, val color: Int)
@Serializable
data class BackupCategory(val id: Long, val name: String, val parentId: Long?)
@Serializable
data class BackupItem(val id: Long, val name: String)
@Serializable
data class BackupPictogram(val id: Long, val name: String, val icon: String)
@Serializable
data class BackupPerson(val id: Long, val name: String, val gender: String)
