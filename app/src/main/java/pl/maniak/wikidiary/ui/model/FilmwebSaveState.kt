package pl.maniak.wikidiary.ui.model

sealed interface FilmwebSaveState {
    data object Idle : FilmwebSaveState
    data object Saving : FilmwebSaveState
    data class Error(val message: String) : FilmwebSaveState
}
