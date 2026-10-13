package pl.maniak.wikidiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_table")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String,
    val isActive: Boolean = true
)
