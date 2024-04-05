package pl.maniak.wikidiary.ui.model

sealed class BottomSheetUiState {
    data object CreateProject : BottomSheetUiState()
    data object None : BottomSheetUiState()
}