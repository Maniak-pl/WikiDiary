package pl.maniak.wikidiary.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "film_table",
    indices = [Index(value = ["canonicalUrl"], unique = true)]
)
data class FilmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val canonicalUrl: String,
    val title: String,
    val year: Int,
    val mediaType: String,
    val posterUrl: String? = null
)
