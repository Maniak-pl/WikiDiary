package pl.maniak.wikidiary.ui.model

import pl.maniak.wikidiary.data.Tag

sealed interface BottomSheetUiState {
    data class CreateProject(val tag: Tag? = null) : BottomSheetUiState
    data object CreateCategory : BottomSheetUiState
    data object None : BottomSheetUiState
}