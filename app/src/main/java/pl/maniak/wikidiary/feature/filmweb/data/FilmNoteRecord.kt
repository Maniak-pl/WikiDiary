package pl.maniak.wikidiary.feature.filmweb.data

data class FilmNoteRecord(
    val noteId: Long,
    val canonicalUrl: String,
    val title: String,
    val year: Int,
    val mediaType: String,
    val posterUrl: String?
)
