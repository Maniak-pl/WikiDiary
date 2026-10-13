package pl.maniak.wikidiary.ui

import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Bottom sheet shown above the current tab; saved as JSON so it survives rotation. */
@Serializable
sealed interface AppSheet {
    @Serializable data class TagEditor(val tagId: Long? = null) : AppSheet
    @Serializable data object Categories : AppSheet
    @Serializable data class Filmweb(val query: String = "") : AppSheet
    @Serializable data object Pictograms : AppSheet
    @Serializable data object People : AppSheet
    @Serializable data object None : AppSheet

    companion object {
        val Saver: Saver<AppSheet, String> = Saver(
            save = { Json.encodeToString(serializer(), it) },
            restore = { Json.decodeFromString(serializer(), it) }
        )
    }
}
