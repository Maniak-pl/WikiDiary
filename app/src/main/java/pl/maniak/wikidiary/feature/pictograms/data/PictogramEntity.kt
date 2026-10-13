package pl.maniak.wikidiary.feature.pictograms.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pictogram_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class PictogramEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String
)
