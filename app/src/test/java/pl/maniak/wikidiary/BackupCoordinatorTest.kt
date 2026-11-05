package pl.maniak.wikidiary

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.backup.*
import pl.maniak.wikidiary.feature.diary.data.TagEntity
import pl.maniak.wikidiary.testing.inMemoryDatabase
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class BackupCoordinatorTest {
    private val database = inMemoryDatabase()
    private val store = ConfigurationStore(database)
    private val disk = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("backup-test", Context.MODE_PRIVATE)
    private val preferences = BackupPreferences(disk)
    private val cloud = Cloud()
    private var pendingDocument = false
    private var requestedAccount = ""
    private val coordinator = BackupCoordinator(store, cloud, preferences, {
        requestedAccount = it
        "token"
    }, { pendingDocument })

    @After fun close() {
        disk.edit().clear().commit()
        database.close()
    }

    @Test fun freshInstallationCannotOverwriteAnExistingGoogleCopy() = runBlocking {
        val saved = store.snapshot().copy(tags = listOf(BackupTag(1, "Saved", null, 1, 123)))
        cloud.remote = RemoteBackup("file", BackupFile(10, saved))
        coordinator.connect("chosen-account", "token")
        coordinator.save()
        assertEquals(0, cloud.writes)
        assertFalse(preferences.state.value.enabled)
        assertTrue(preferences.state.value.restoreAvailable)
        assertTrue(store.snapshot().tags.isEmpty())
        coordinator.restore("token")
        assertEquals(saved, store.snapshot())
        assertTrue(preferences.state.value.enabled)
    }

    @Test fun connectCreatesFirstCopyThenUpdatesChangesAndDeletions() = runBlocking {
        database.tagDao().insert(TagEntity(1, "Initial"))
        coordinator.connect("chosen-account", "token")
        assertEquals(1, cloud.writes)
        database.tagDao().deleteAll()
        coordinator.save()
        assertEquals(2, cloud.writes)
        assertTrue(requireNotNull(cloud.remote).file.data.tags.isEmpty())
        assertEquals("chosen-account", requestedAccount)
        coordinator.save()
        assertEquals(2, cloud.writes)
    }

    @Test fun restoreLatestCopyDoesNotUndoAnAutomaticallySavedTagDeletion() = runBlocking {
        database.tagDao().insert(TagEntity(1, "Colored tag", color = 0xFF673AB7.toInt()))
        coordinator.connect("chosen-account", "token")
        val beforeDeletion = requireNotNull(cloud.remote).file.data
        assertEquals(0xFF673AB7.toInt(), beforeDeletion.tags.single().color)

        database.tagDao().deleteById(1)
        coordinator.save()
        coordinator.restore("token")

        assertTrue(store.snapshot().tags.isEmpty())
        assertEquals(2, cloud.writes)
    }

    @Test fun externallyChangedBackupIsNotOverwritten() = runBlocking {
        coordinator.connect("chosen-account", "token")
        val initial = requireNotNull(cloud.remote)
        cloud.remote = initial.copy(file = initial.file.copy(data = initial.file.data.copy(
            tags = listOf(BackupTag(1, "Other device", null, 1, 1)))))
        database.tagDao().insert(TagEntity(1, "Local"))
        assertThrows(IllegalArgumentException::class.java) { runBlocking { coordinator.save() } }
        assertEquals(1, cloud.writes)
    }

    @Test fun interruptedUploadCanBeRetriedWithoutLosingLaterLocalEdits() = runBlocking {
        coordinator.connect("chosen-account", "token")
        database.tagDao().insert(TagEntity(1, "First"))
        cloud.failAfterWrite = true
        assertThrows(IOException::class.java) { runBlocking { coordinator.save() } }
        assertTrue(preferences.state.value.attemptedHash.isNotBlank())
        database.tagDao().insert(TagEntity(1, "Second"))
        cloud.failAfterWrite = false
        coordinator.save()
        assertEquals("Second", requireNotNull(cloud.remote).file.data.tags.single().name)
        assertEquals("", preferences.state.value.attemptedHash)
    }

    @Test fun pendingUploadPreventsRestoreAndDisconnectNeverDeletesCloudCopy() = runBlocking {
        coordinator.connect("chosen-account", "token")
        pendingDocument = true
        assertThrows(IllegalArgumentException::class.java) { runBlocking { coordinator.restore("token") } }
        coordinator.disconnect()
        coordinator.save()
        assertNotNull(cloud.remote)
        assertEquals("chosen-account", preferences.state.value.account)
        assertFalse(preferences.state.value.enabled)
        assertEquals(1, cloud.writes)
    }

    @Test fun backupSettingsPersistWithoutAccessTokens() = runBlocking {
        coordinator.connect("chosen-account", "secret-access-token")
        assertEquals(preferences.state.value, BackupPreferences(disk).state.value)
        assertFalse(disk.all.values.any { it.toString().contains("secret-access-token") })
    }

    @Test fun disappearingCloudCopyIsNotSilentlyRecreated() = runBlocking {
        coordinator.connect("chosen-account", "token")
        cloud.remote = null
        assertThrows(IllegalArgumentException::class.java) { runBlocking { coordinator.save() } }
        assertEquals(1, cloud.writes)
    }

    @Test fun reconnectingSameKnownAccountDoesNotRequireRestoringOverLocalData() = runBlocking {
        coordinator.connect("chosen-account", "token")
        database.tagDao().insert(TagEntity(1, "Local change"))
        coordinator.connect("chosen-account", "new-token")
        assertTrue(preferences.state.value.enabled)
        assertFalse(preferences.state.value.restoreAvailable)
        assertTrue(preferences.state.value.dirty)
        coordinator.save()
        assertEquals("Local change", requireNotNull(cloud.remote).file.data.tags.single().name)
    }

    @Test fun changingAccountDoesNotUsePreviousAccountsBackupBaseline() = runBlocking {
        coordinator.connect("first-account", "token")
        coordinator.connect("second-account", "token")
        assertFalse(preferences.state.value.enabled)
        assertTrue(preferences.state.value.restoreAvailable)
        assertEquals("second-account", preferences.state.value.account)
        assertEquals(1, cloud.writes)
    }

    @Test fun interruptedFirstUploadCanBeRecognizedOnReconnectWithoutRestoring() = runBlocking {
        cloud.failAfterWrite = true
        assertThrows(IOException::class.java) {
            runBlocking { coordinator.connect("chosen-account", "token") }
        }
        assertFalse(preferences.state.value.enabled)
        database.tagDao().insert(TagEntity(1, "Later change"))
        cloud.failAfterWrite = false
        coordinator.connect("chosen-account", "token")
        assertTrue(preferences.state.value.enabled)
        assertFalse(preferences.state.value.restoreAvailable)
        coordinator.save()
        assertEquals("Later change", requireNotNull(cloud.remote).file.data.tags.single().name)
    }

    @Test fun pausedBackupCanResumeWithoutRestoreAndIncludesChangesMadeWhilePaused() = runBlocking {
        coordinator.connect("chosen-account", "token")
        coordinator.disconnect()
        database.tagDao().insert(TagEntity(1, "Changed while paused"))
        coordinator.connect("chosen-account", "token")
        assertTrue(preferences.state.value.enabled)
        assertFalse(preferences.state.value.restoreAvailable)
        coordinator.save()
        assertEquals("Changed while paused", requireNotNull(cloud.remote).file.data.tags.single().name)
    }

    @Test fun explicitLocalCopyUsesLocalConfigurationWithoutRestoringCloudData() = runBlocking {
        cloud.remote = RemoteBackup("old", BackupFile(1, store.snapshot()))
        coordinator.connect("chosen-account", "token")
        assertTrue(preferences.state.value.restoreAvailable)
        database.tagDao().insert(TagEntity(1, "Local"))
        coordinator.saveLocalCopy("token")
        assertEquals("Local", requireNotNull(cloud.remote).file.data.tags.single().name)
        assertTrue(preferences.state.value.enabled)
        assertFalse(preferences.state.value.restoreAvailable)
    }

    @Test fun editsMadeDuringUploadRemainPendingAndAreSavedOnRetry() = runBlocking {
        coordinator.connect("chosen-account", "token")
        database.tagDao().insert(TagEntity(1, "First"))
        cloud.afterWrite = { database.tagDao().insert(TagEntity(1, "Edited during upload")) }
        assertFalse(coordinator.save())
        assertTrue(preferences.state.value.dirty)
        cloud.afterWrite = null
        assertTrue(coordinator.save())
        assertEquals("Edited during upload", requireNotNull(cloud.remote).file.data.tags.single().name)
        assertFalse(preferences.state.value.dirty)
    }

    private class Cloud : BackupCloud {
        var remote: RemoteBackup? = null
        var writes = 0
        var failAfterWrite = false
        var afterWrite: (() -> Unit)? = null
        override fun read(token: String) = remote
        override fun write(token: String, file: BackupFile) {
            writes++
            remote = RemoteBackup("file-$writes", file)
            afterWrite?.invoke()
            if (failAfterWrite) throw IOException("Interrupted response")
        }
    }
}
