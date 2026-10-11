package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.maniak.wikidiary.data.database.PictogramDao
import pl.maniak.wikidiary.data.database.PictogramEntryDao
import pl.maniak.wikidiary.data.database.PictogramEntryEntity
import pl.maniak.wikidiary.data.database.PictogramEntryRecord
import pl.maniak.wikidiary.data.database.PictogramEntity
import pl.maniak.wikidiary.data.mapper.PictogramEntryMapper
import pl.maniak.wikidiary.data.mapper.PictogramMapper
import pl.maniak.wikidiary.domain.model.Pictogram
import pl.maniak.wikidiary.domain.model.PictogramEntry
import pl.maniak.wikidiary.domain.repository.PictogramRepository
import pl.maniak.wikidiary.domain.repository.PictogramRepositoryImpl

class PictogramRepositoryTest {

    @Test
    fun pictogramsCanBeCreatedUpdatedAttachedAndDeleted() = runBlocking {
        val pictogramDao = InMemoryPictogramDao()
        val entryDao = InMemoryPictogramEntryDao(pictogramDao)
        val repository: PictogramRepository = PictogramRepositoryImpl(
            pictogramDao = pictogramDao,
            pictogramEntryDao = entryDao,
            pictogramMapper = PictogramMapper(),
            pictogramEntryMapper = PictogramEntryMapper()
        )

        repository.savePictogram(Pictogram(name = "love", icon = "❤️"))
        val created = repository.getPictograms().single()

        repository.savePictogram(created.copy(name = "love-updated", icon = "💖"))
        repository.addPictogramEntry(
            PictogramEntry(
                pictogramId = created.id,
                tag = "Today",
                date = Date(1_728_480_000_000),
                name = "love-updated",
                icon = "💖"
            )
        )

        assertEquals("love-updated", repository.getPictograms().single().name)
        assertEquals("Today", repository.getPictogramEntries().single().tag)
        assertEquals("love-updated", repository.getPictogramEntries().single().name)

        repository.deletePictogramEntry(repository.getPictogramEntries().single().id)
        assertTrue(repository.getPictogramEntries().isEmpty())

        repository.addPictogramEntry(
            PictogramEntry(
                pictogramId = created.id,
                tag = "Today",
                date = Date(1_728_480_000_000),
                name = "love-updated",
                icon = "💖"
            )
        )
        repository.deletePictogram(created.id)

        assertTrue(repository.getPictograms().isEmpty())
        assertTrue(repository.getPictogramEntries().isEmpty())
    }
}

private class InMemoryPictogramDao : PictogramDao {
    private val values = linkedMapOf<Long, PictogramEntity>()
    private var nextId = 1L

    override fun getAll(): List<PictogramEntity> = values.values.toList()

    override fun getById(id: Long): PictogramEntity? = values[id]

    override fun insert(pictogram: PictogramEntity): Long {
        val id = if (pictogram.id == 0L) nextId++ else pictogram.id
        values[id] = pictogram.copy(id = id)
        return id
    }

    override fun update(pictogram: PictogramEntity) {
        values[pictogram.id] = pictogram
    }

    override fun deleteById(id: Long) {
        values.remove(id)
    }
}

private class InMemoryPictogramEntryDao(
    private val pictogramDao: PictogramDao
) : PictogramEntryDao {
    private val values = linkedMapOf<Long, PictogramEntryEntity>()
    private var nextId = 1L

    override fun getAll(): List<PictogramEntryRecord> {
        return values.values.map { entry ->
            val pictogram = pictogramDao.getById(entry.pictogramId)
                ?: error("Pictogram is missing")
            PictogramEntryRecord(
                id = entry.id,
                pictogramId = entry.pictogramId,
                tag = entry.tag,
                category = entry.category,
                date = entry.date,
                name = pictogram.name,
                icon = pictogram.icon
            )
        }
    }

    override fun insert(entry: PictogramEntryEntity): Long {
        val id = if (entry.id == 0L) nextId++ else entry.id
        values[id] = entry.copy(id = id)
        return id
    }

    override fun deleteById(id: Long) {
        values.remove(id)
    }

    override fun deleteByPictogramId(pictogramId: Long) {
        values.values.removeAll { it.pictogramId == pictogramId }
    }
}
