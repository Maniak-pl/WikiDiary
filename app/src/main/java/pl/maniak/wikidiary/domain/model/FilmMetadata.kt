package pl.maniak.wikidiary.domain.model

enum class FilmMediaType {
    FILM,
    SERIAL
}

data class FilmMetadata(
    val canonicalUrl: String,
    val title: String,
    val year: Int,
    val mediaType: FilmMediaType,
    val posterUrl: String? = null
)

data class FilmNoteEntry(
    val noteId: Long,
    val metadata: FilmMetadata
)
