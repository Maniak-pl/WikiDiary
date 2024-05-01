package pl.maniak.wikidiary.ui.model

import androidx.compose.ui.graphics.Color
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote

sealed class ActionClick {
    data class AddNote(val tag: Tag, val content: String) : ActionClick()

    data class AddTag(val tag: String) : ActionClick()

    data class DeleteNote(val note: WikiNote) : ActionClick()

    data class DeleteTag(val id: Long) : ActionClick()

    data object TagCreateProject : ActionClick()

    data class ConfirmProject(
        val id: Long,
        val name: String,
        val category: String,
        val color: Color,
    ) : ActionClick()

    data object TagCreateCategory : ActionClick()

    data class AddCategory(val name: String) : ActionClick()

    data class DeleteCategory(val id: Long) : ActionClick()

    data class UpdateCategory(val id: Long, val name: String) : ActionClick()

    data class EditTag(val tag: Tag) : ActionClick()
}