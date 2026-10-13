package pl.maniak.wikidiary.feature.filmweb.domain


interface FilmwebSearchRepository {
    suspend fun search(query: String): List<FilmSearchResult>
}
