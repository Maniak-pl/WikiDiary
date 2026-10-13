package pl.maniak.wikidiary.feature.diary.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tag_table")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var tag: String,
    var category: String? = null,
    var date: Date = Date(),
    var color: Int = 16777216,
)