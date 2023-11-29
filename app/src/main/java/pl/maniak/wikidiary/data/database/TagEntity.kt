package pl.maniak.wikidiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_table")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var tag: String
)