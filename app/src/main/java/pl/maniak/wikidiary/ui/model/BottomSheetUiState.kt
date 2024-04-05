package pl.maniak.wikidiary.ui.model

sealed interface BottomSheetUiState {
    data object CreateProject : BottomSheetUiState
    data object None : BottomSheetUiState
}