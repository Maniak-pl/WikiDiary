package pl.maniak.wikidiary.feature.pictograms.data

data class PictogramEntryRecord(
    val id: Long,
    val pictogramId: Long,
    val tag: String,
    val category: String?,
    val date: Long,
    val name: String,
    val icon: String
)
