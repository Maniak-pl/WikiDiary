package pl.maniak.wikidiary.feature.pictograms.domain


interface PictogramRepository {
    suspend fun getPictograms(): List<Pictogram>
    suspend fun savePictogram(pictogram: Pictogram)
    suspend fun deletePictogram(id: Long)
    suspend fun getPictogramEntries(): List<PictogramEntry>
    suspend fun addPictogramEntry(entry: PictogramEntry)
    suspend fun deletePictogramEntry(id: Long)
}
