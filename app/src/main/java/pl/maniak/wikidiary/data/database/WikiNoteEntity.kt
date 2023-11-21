package pl.maniak.wikidiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wiki_note_table")
data class WikiNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tag: String,
    val content: String,
    val date: Long,
    val isSend: Boolean
)