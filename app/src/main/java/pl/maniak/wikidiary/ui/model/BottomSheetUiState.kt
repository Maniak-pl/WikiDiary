package pl.maniak.wikidiary.ui.model

sealed interface BottomSheetUiState {
    data object CreateProject : BottomSheetUiState
    data object CreateCategory : BottomSheetUiState
    data object None : BottomSheetUiState
}