package pl.maniak.wikidiary.feature.pictograms.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pictogram_entry_table",
    foreignKeys = [
        ForeignKey(
            entity = PictogramEntity::class,
            parentColumns = ["id"],
            childColumns = ["pictogramId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pictogramId"]),
        Index(value = ["date", "tag"])
    ]
)
data class PictogramEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pictogramId: Long,
    val tag: String,
    val category: String? = null,
    val date: Long
)
