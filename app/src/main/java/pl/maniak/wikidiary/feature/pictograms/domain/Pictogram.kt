package pl.maniak.wikidiary.feature.pictograms.domain

import java.util.Date

data class Pictogram(
    val id: Long = 0,
    val name: String,
    val icon: String
)

data class PictogramEntry(
    val id: Long = 0,
    val pictogramId: Long,
    val tag: String,
    val category: String? = null,
    val date: Date,
    val name: String,
    val icon: String
)
