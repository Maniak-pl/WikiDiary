package pl.maniak.wikidiary.ui

import pl.maniak.wikidiary.feature.diary.domain.Tag

sealed interface AppSheet {
    data class TagEditor(val tag: Tag? = null) : AppSheet
    data object Categories : AppSheet
    data object Filmweb : AppSheet
    data object Pictograms : AppSheet
    data object People : AppSheet
    data object None : AppSheet
}
