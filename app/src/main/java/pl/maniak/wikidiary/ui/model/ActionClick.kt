package pl.maniak.wikidiary.ui.model

import androidx.compose.ui.graphics.Color
import pl.maniak.wikidiary.data.Routine
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote
import pl.maniak.wikidiary.domain.model.FilmMetadata
import java.util.Date

sealed class ActionClick {
    data class AddNote(val tag: Tag, val content: String) : ActionClick()

    data class AddTag(val tag: String) : ActionClick()

    data class DeleteNote(val note: WikiNote) : ActionClick()

    data class DeleteTag(val id: Long) : ActionClick()

    data object TagCreateProject : ActionClick()

    data object TagCreateFilmweb : ActionClick()

    data object TagCreatePictogram : ActionClick()

    data class SavePictogram(
        val id: Long,
        val name: String,
        val icon: String
    ) : ActionClick()

    data class DeletePictogram(val id: Long) : ActionClick()

    data class AddPictogramToTag(
        val pictogramId: Long,
        val tag: Tag,
        val date: Date
    ) : ActionClick()

    data class DeletePictogramEntry(val id: Long) : ActionClick()

    data class ConfirmProject(
        val id: Long,
        val name: String,
        val category: String,
        val color: Color,
    ) : ActionClick()

    data object TagCreateCategory : ActionClick()

    data object TagChangeDate : ActionClick()

    data class AddCategory(val name: String) : ActionClick()

    data class DeleteCategory(val id: Long) : ActionClick()

    data class UpdateCategory(val id: Long, val name: String) : ActionClick()

    data class EditTag(val tag: Tag) : ActionClick()

    data class AddRoutine(val name: String) : ActionClick()

    data class DeleteRoutine(val id: Long) : ActionClick()

    data class UpdateRoutine(val routine: Routine) : ActionClick()

    data class DataPickerChangeDate(val date: Date) : ActionClick()

    data class SaveFilmNote(
        val tag: Tag,
        val date: Date,
        val metadata: FilmMetadata,
        val sentencePrefix: String
    ) : ActionClick()

    data object CloseBottomSheet : ActionClick()

    data class EditNote(val note: WikiNote) : ActionClick()

    data class DeleteNotes(val notes: List<Long>) : ActionClick()
}
