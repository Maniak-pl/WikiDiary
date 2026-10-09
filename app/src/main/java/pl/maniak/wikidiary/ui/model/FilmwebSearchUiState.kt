package pl.maniak.wikidiary.ui.model

import pl.maniak.wikidiary.domain.model.FilmSearchResult

data class FilmwebSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<FilmSearchResult> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false
)
