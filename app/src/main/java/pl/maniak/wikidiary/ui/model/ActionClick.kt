package pl.maniak.wikidiary.ui.model

import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote

sealed class ActionClick {
    data class AddNote(val note: WikiNote) : ActionClick()

    data class AddTag(val tag: Tag) : ActionClick()

    data class DeleteNote(val note: WikiNote) : ActionClick()

    data class DeleteTag(val tag: Tag) : ActionClick()

    data object TagCreateProject : ActionClick()

    data class ConfirmCreateProject(
        val name: String,
        val category: String
    ) : ActionClick()

    data object TagCreateCategory : ActionClick()

    data class AddCategory(val name: String) : ActionClick()

    data class DeleteCategory(val id: Long) : ActionClick()

    data class UpdateCategory(val id: Long, val name: String) : ActionClick()
}