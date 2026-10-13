package pl.maniak.wikidiary

import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter
import pl.maniak.wikidiary.feature.pictograms.data.PictogramEntryMapper
import pl.maniak.wikidiary.feature.pictograms.data.PictogramMapper
import pl.maniak.wikidiary.feature.pictograms.domain.Pictogram
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramRepository
import pl.maniak.wikidiary.feature.pictograms.data.PictogramRepositoryImpl

@RunWith(RobolectricTestRunner::class)
class PictogramRepositoryTest {

    private val database = inMemoryDatabase()
    private val repository: PictogramRepository = PictogramRepositoryImpl(
        pictogramDao = database.pictogramDao(),
        pictogramEntryDao = database.pictogramEntryDao(),
        pictogramMapper = PictogramMapper(),
        pictogramEntryMapper = PictogramEntryMapper()
    )

    @After
    fun tearDown() = database.close()

    @Test
    fun pictogramsCanBeCreatedUpdatedAttachedAndDeleted() = runBlocking {

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

    @Test
    fun observedPictogramsAndEntriesReflectChanges() = runBlocking {
        val pictograms = repository.observePictograms().nextValueAfter {
            repository.savePictogram(Pictogram(name = "love", icon = "❤️"))
        }
        val love = pictograms.single()

        val entries = repository.observePictogramEntries().nextValueAfter {
            repository.addPictogramEntry(
                PictogramEntry(
                    pictogramId = love.id,
                    tag = "Today",
                    date = Date(1_728_480_000_000),
                    name = love.name,
                    icon = love.icon
                )
            )
        }

        assertEquals("love", love.name)
        assertEquals(listOf("Today"), entries.map(PictogramEntry::tag))
    }
}
