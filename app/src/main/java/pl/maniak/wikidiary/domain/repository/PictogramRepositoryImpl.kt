package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.database.PictogramDao
import pl.maniak.wikidiary.data.database.PictogramEntryDao
import pl.maniak.wikidiary.data.mapper.PictogramEntryMapper
import pl.maniak.wikidiary.data.mapper.PictogramMapper
import pl.maniak.wikidiary.domain.model.Pictogram
import pl.maniak.wikidiary.domain.model.PictogramEntry

class PictogramRepositoryImpl(
    private val pictogramDao: PictogramDao,
    private val pictogramEntryDao: PictogramEntryDao,
    private val pictogramMapper: PictogramMapper,
    private val pictogramEntryMapper: PictogramEntryMapper
) : PictogramRepository {

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
