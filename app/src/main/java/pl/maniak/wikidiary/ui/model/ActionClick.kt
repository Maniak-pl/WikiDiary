package pl.maniak.wikidiary.ui.model

import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.WikiNote

sealed class ActionClick {
    data class AddNote(val note: WikiNote) : ActionClick()
    data class AddTag(val tag: Tag) : ActionClick()
    data class DeleteNote(val note: WikiNote) : ActionClick()
    data class DeleteTag(val tag: Tag) : ActionClick()
}