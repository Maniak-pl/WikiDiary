package pl.maniak.wikidiary.feature.filmweb.ui

import pl.maniak.wikidiary.feature.filmweb.domain.FilmSearchResult

data class FilmwebSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<FilmSearchResult> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false
)
