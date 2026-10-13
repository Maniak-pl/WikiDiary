package pl.maniak.wikidiary.feature.filmweb.ui

sealed interface FilmwebSaveState {
    data object Idle : FilmwebSaveState
    data object Saving : FilmwebSaveState
    data object Saved : FilmwebSaveState
    data class Error(val message: String) : FilmwebSaveState
}
