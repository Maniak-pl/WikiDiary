package pl.maniak.wikidiary.feature.filmweb.domain

data class FilmSearchResult(
    val id: Int,
    val title: String,
    val year: Int?,
    val mediaType: FilmMediaType,
    val posterUrl: String?,
    val canonicalUrl: String
)
