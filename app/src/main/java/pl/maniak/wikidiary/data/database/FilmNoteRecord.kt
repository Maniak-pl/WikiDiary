package pl.maniak.wikidiary.data.database

data class FilmNoteRecord(
    val noteId: Long,
    val canonicalUrl: String,
    val title: String,
    val year: Int,
    val mediaType: String,
    val posterUrl: String?
)
