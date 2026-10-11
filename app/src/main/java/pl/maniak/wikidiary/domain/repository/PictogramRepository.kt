package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.domain.model.Pictogram
import pl.maniak.wikidiary.domain.model.PictogramEntry

interface PictogramRepository {
    suspend fun getPictograms(): List<Pictogram>
    suspend fun savePictogram(pictogram: Pictogram)
    suspend fun deletePictogram(id: Long)
    suspend fun getPictogramEntries(): List<PictogramEntry>
    suspend fun addPictogramEntry(entry: PictogramEntry)
    suspend fun deletePictogramEntry(id: Long)
}
