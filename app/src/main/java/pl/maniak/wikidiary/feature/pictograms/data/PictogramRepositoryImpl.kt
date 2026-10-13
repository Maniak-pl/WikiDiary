package pl.maniak.wikidiary.feature.pictograms.data

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.pictograms.domain.Pictogram
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry

class PictogramRepositoryImpl(
    private val pictogramDao: PictogramDao,
    private val pictogramEntryDao: PictogramEntryDao,
    private val pictogramMapper: PictogramMapper,
    private val pictogramEntryMapper: PictogramEntryMapper
) : PictogramRepository {
    override fun observePictograms(): Flow<List<Pictogram>> =
        pictogramDao.observeAll().map { pictograms -> pictograms.map(pictogramMapper::mapToDomain) }

    override fun observePictogramEntries(): Flow<List<PictogramEntry>> =
        pictogramEntryDao.observeAll().map { entries -> entries.map(pictogramEntryMapper::mapToDomain) }


    override suspend fun getPictograms(): List<Pictogram> = withContext(Dispatchers.IO) {
        pictogramDao.getAll().map(pictogramMapper::mapToDomain)
    }

    override suspend fun savePictogram(pictogram: Pictogram) = withContext(Dispatchers.IO) {
        if (pictogram.id == 0L) {
            pictogramDao.insert(pictogramMapper.mapToEntity(pictogram))
        } else {
            pictogramDao.update(pictogramMapper.mapToEntity(pictogram))
        }
        Unit
    }

    override suspend fun deletePictogram(id: Long) = withContext(Dispatchers.IO) {
        pictogramEntryDao.deleteByPictogramId(id)
        pictogramDao.deleteById(id)
    }

    override suspend fun getPictogramEntries(): List<PictogramEntry> =
        withContext(Dispatchers.IO) {
            pictogramEntryDao.getAll().map(pictogramEntryMapper::mapToDomain)
        }

    override suspend fun addPictogramEntry(entry: PictogramEntry) =
        withContext(Dispatchers.IO) {
            pictogramEntryDao.insert(pictogramEntryMapper.mapToEntity(entry))
            Unit
        }

    override suspend fun deletePictogramEntry(id: Long) = withContext(Dispatchers.IO) {
        pictogramEntryDao.deleteById(id)
    }
}
