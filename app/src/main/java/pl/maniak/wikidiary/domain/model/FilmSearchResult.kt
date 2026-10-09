package pl.maniak.wikidiary.domain.model

data class FilmSearchResult(
    val id: Int,
    val title: String,
    val year: Int?,
    val mediaType: FilmMediaType,
    val posterUrl: String?,
    val canonicalUrl: String
)
