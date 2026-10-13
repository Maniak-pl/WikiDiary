package pl.maniak.wikidiary.feature.pictograms.domain

import kotlinx.coroutines.flow.Flow

interface PictogramRepository {
    fun observePictograms(): Flow<List<Pictogram>>
    fun observePictogramEntries(): Flow<List<PictogramEntry>>
    suspend fun getPictograms(): List<Pictogram>
    suspend fun savePictogram(pictogram: Pictogram)
    suspend fun deletePictogram(id: Long)
    suspend fun getPictogramEntries(): List<PictogramEntry>
    suspend fun addPictogramEntry(entry: PictogramEntry)
    suspend fun deletePictogramEntry(id: Long)
}
