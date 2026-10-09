package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.domain.model.FilmSearchResult

interface FilmwebSearchRepository {
    suspend fun search(query: String): List<FilmSearchResult>
}
