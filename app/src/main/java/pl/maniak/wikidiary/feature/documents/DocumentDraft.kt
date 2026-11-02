package pl.maniak.wikidiary.feature.documents

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import pl.maniak.wikidiary.feature.pictograms.domain.documentImageFile
import pl.maniak.wikidiary.feature.diary.domain.WikiNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DocumentField { FILE, SUBJECT, DESCRIPTION, TAG, CATEGORY }

@Serializable
data class DocumentDraft(
    val uri: String,
    val mimeType: String,
    val extension: String,
    val subject: String,
    val description: String,
    val date: Long,
    val tag: String,
    @SerialName("tagCategory") val documentCategory: String?,
    val imageFile: String = documentImageFile(documentCategory.orEmpty()),
    val driveId: String = "",
    val noteCategory: String? = null,
) {
    val label: String get() = "${subject.trim()} - ${description.trim()}$extension"
    val fileName: String
        get() = "${SimpleDateFormat("yyyy.MM.dd", Locale.ROOT).format(Date(date))} - $label"

    fun validationErrors(): Map<DocumentField, String> = buildMap {
        if (uri.isBlank()) put(DocumentField.FILE, "Wybierz plik z urządzenia.")
        else if (!mimeType.matches(Regex("[A-Za-z0-9!#$&^_.+-]+/[A-Za-z0-9!#$&^_.+-]+"))) {
            put(DocumentField.FILE, "Niepoprawny typ pliku.")
        } else if (extension.hasInvalidNameCharacters()) {
            put(DocumentField.FILE, "Rozszerzenie pliku zawiera niedozwolone znaki.")
        }
        if (subject.isBlank()) put(DocumentField.SUBJECT, "Wpisz podmiot, np. LuxMed.")
        else if (subject.hasInvalidNameCharacters()) put(DocumentField.SUBJECT, "Nazwa zawiera niedozwolone znaki.")
        if (description.isBlank()) put(DocumentField.DESCRIPTION, "Wpisz opis dokumentu.")
        else if (description.hasInvalidNameCharacters()) put(DocumentField.DESCRIPTION, "Opis zawiera niedozwolone znaki.")
        if (tag.isBlank()) put(DocumentField.TAG, "Wybierz tag wpisu.")
        if (documentCategory.isNullOrBlank()) put(DocumentField.CATEGORY, "Wybierz katalog dokumentu.")
        else if (!imageFile.matches(Regex("[\\p{L}\\p{N}_-]+\\.png"))) {
            put(DocumentField.CATEGORY, "Nazwa katalogu nie pozwala utworzyć nazwy grafiki w Wiki.")
        }
    }

    fun validate() {
        require(validationErrors().isEmpty()) { validationErrors().values.joinToString("\n") }
    }

    private fun String.hasInvalidNameCharacters(): Boolean =
        any { it.isISOControl() || it in "|[]{}\\/" }

    fun note(): WikiNote {
        require(driveId.matches(Regex("[A-Za-z0-9_-]+"))) { "Brak identyfikatora pliku Google Drive." }
        return WikiNote(
            tag = tag,
            category = noteCategory,
            date = Date(date),
            content = "**📋 Dokumentacja:**\n" +
                "      * [[https://drive.google.com/open?id=$driveId|$label]]\n\n" +
                "{{ :$imageFile?200 |}}",
        )
    }
}
