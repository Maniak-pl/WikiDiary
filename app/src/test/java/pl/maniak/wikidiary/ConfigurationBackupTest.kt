package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.backup.*
import pl.maniak.wikidiary.feature.diary.data.CategoryEntity
import pl.maniak.wikidiary.feature.diary.data.TagEntity
import pl.maniak.wikidiary.feature.diary.data.WikiNoteEntity
import pl.maniak.wikidiary.feature.people.data.PersonEntity
import pl.maniak.wikidiary.feature.pictograms.data.PictogramEntity
import pl.maniak.wikidiary.feature.pictograms.data.PictogramEntryEntity
import pl.maniak.wikidiary.feature.routines.data.RoutineEntity
import pl.maniak.wikidiary.feature.todo.data.TodoEntity
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.testing.inMemoryDatabase
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class ConfigurationBackupTest {
    private val database = inMemoryDatabase()
    private val store = ConfigurationStore(database)

    @After fun close() = database.close()

    private fun seed() {
        database.categoryDao().insert(CategoryEntity(10, "Work"))
        database.tagDao().insert(TagEntity(20, "Project", "Work", Date(12345), 0x123456))
        database.todoDao().insert(TodoEntity(30, "Pending"))
        database.todoDao().insert(TodoEntity(31, "Completed private history", true))
        database.routineDao().insert(RoutineEntity(40, "Walk", true))
        database.pictogramDao().insert(PictogramEntity(50, "doc_work", "icon"))
        database.personDao().insert(PersonEntity(60, "Person", "female"))
        database.personDao().insert(PersonEntity(61, "Archived person", "male", false))
    }

    @Test fun snapshotContainsOnlyConfigurationAndNeverDailyEntries() = runBlocking {
        seed()
        database.wikiNoteDao().insert(WikiNoteEntity(tag = "Project",
            content = "PRIVATE NOTE", folder = "Work", date = 100, isSend = false))
        database.pictogramEntryDao().insert(PictogramEntryEntity(
            pictogramId = 50, tag = "PRIVATE ENTRY", category = "Work", date = 100))
        val snapshot = store.snapshot()
        val json = BackupFile(100, snapshot).encode()
        assertEquals(listOf("Pending"), snapshot.todos.map { it.name })
        assertEquals(listOf("Person"), snapshot.people.map { it.name })
        assertFalse(json.contains("PRIVATE"))
        assertFalse(json.contains("Completed private history"))
        assertFalse(json.contains("Archived person"))
        assertFalse(json.contains("isCompleted"))
        assertEquals(snapshot, BackupFile.decode(json).data)
    }

    @Test fun restoreReplacesConfigurationPreservesRelationshipsAndResetsDailyFlags() = runBlocking {
        seed()
        val snapshot = store.snapshot()
        database.tagDao().deleteById(20)
        database.tagDao().insert(TagEntity(21, "Temporary"))
        database.todoDao().insert(TodoEntity(32, "Temporary task"))
        store.restore(snapshot)
        assertEquals(snapshot, store.snapshot())
        assertEquals("Work", database.tagDao().getAll().single().category)
        assertEquals(0x123456, database.tagDao().getAll().single().color)
        assertFalse(database.routineDao().getAllRoutines().single().isCompleted)
        assertEquals(0, database.backupDao().journalEntryCount())
    }

    @Test fun emptyConfigurationRestoresDeletionsInsteadOfReintroducingDefaults() = runBlocking {
        val empty = store.snapshot()
        seed()
        store.restore(empty)
        assertEquals(empty, store.snapshot())
    }

    @Test fun restoreWithExistingNotesIsRejectedBeforeAnyDataIsChanged() = runBlocking {
        val empty = store.snapshot()
        seed()
        database.wikiNoteDao().insert(WikiNoteEntity(tag = "Project", content = "Keep",
            date = 100, isSend = false))
        val before = store.snapshot()
        assertThrows(IllegalArgumentException::class.java) { runBlocking { store.restore(empty) } }
        assertEquals(before, store.snapshot())
        assertEquals(1, database.wikiNoteDao().getNoteCount())
    }

    @Test fun restoreWithPictogramEntriesIsRejected() = runBlocking {
        val empty = store.snapshot()
        seed()
        database.pictogramEntryDao().insert(PictogramEntryEntity(
            pictogramId = 50, tag = "Today", date = 100))
        assertThrows(IllegalArgumentException::class.java) { runBlocking { store.restore(empty) } }
        assertEquals(1, database.pictogramEntryDao().getAll().size)
    }

    @Test fun unsupportedOrCorruptBackupDoesNotChangeLocalConfiguration() = runBlocking {
        seed()
        val before = store.snapshot()
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { store.restore(before.copy(version = 2)) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            before.copy(pictograms = before.pictograms + BackupPictogram(51, "Doc Work", "other")).validate()
        }
        assertEquals(before, store.snapshot())
    }

    @Test fun hashIgnoresBackupTimestampAndRoutineCompletionButDetectsEdits() = runBlocking {
        seed()
        val before = store.snapshot()
        database.routineDao().updateAll(listOf(RoutineEntity(40, "Walk", false)))
        assertEquals(before.hash(), store.snapshot().hash())
        database.todoDao().update(TodoEntity(30, "Edited"))
        assertNotEquals(before.hash(), store.snapshot().hash())
    }

    @Test fun reinstallRestoresCustomTagsAlongsideTodosAndPeopleInsteadOfOnlyDefaultTags() = runBlocking {
        WikiNoteDatabase.populateInitialData(database)
        database.tagDao().insert(TagEntity(tag = "Custom backup tag", category = "Work", color = 123))
        database.todoDao().insert(TodoEntity(title = "Custom todo"))
        database.personDao().insert(PersonEntity(name = "Custom person", gender = "neutral"))
        val saved = BackupFile(100, store.snapshot()).encode()
        val reinstalled = inMemoryDatabase()
        try {
            WikiNoteDatabase.populateInitialData(reinstalled)
            ConfigurationStore(reinstalled).restore(BackupFile.decode(saved).data)
            assertTrue(reinstalled.tagDao().getAll().any { it.tag == "Custom backup tag" })
            assertEquals("Custom todo", reinstalled.todoDao().getAll().single().title)
            assertEquals("Custom person", reinstalled.personDao().getAll().single().name)
            assertEquals(store.snapshot(), ConfigurationStore(reinstalled).snapshot())
        } finally {
            reinstalled.close()
        }
    }
}
