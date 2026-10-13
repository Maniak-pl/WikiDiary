package pl.maniak.wikidiary.feature.diary.ui

import pl.maniak.wikidiary.feature.diary.domain.Tag

/** Places the diary composer can send the user to; the app shell decides how to show them. */
sealed interface DiaryNavigation {
    data object CreateTag : DiaryNavigation
    data class EditTag(val tag: Tag) : DiaryNavigation
    data object Categories : DiaryNavigation
    data object Filmweb : DiaryNavigation
    data object Pictograms : DiaryNavigation
    data object People : DiaryNavigation
    data object PickDate : DiaryNavigation
}
