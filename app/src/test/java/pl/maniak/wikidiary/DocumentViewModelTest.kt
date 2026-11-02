package pl.maniak.wikidiary

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.domain.Category
import pl.maniak.wikidiary.feature.documents.DocumentDraft
import pl.maniak.wikidiary.feature.documents.DocumentStore
import pl.maniak.wikidiary.feature.documents.DocumentViewModel
import pl.maniak.wikidiary.feature.documents.DriveClient
import pl.maniak.wikidiary.testing.InMemoryConfig
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class DocumentViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = inMemoryDatabase()
    private val preferences = context.getSharedPreferences("document-vm-test", Context.MODE_PRIVATE)
    private val store = DocumentStore(preferences, database)
    private val config = InMemoryConfig()
    private val draft = DocumentDraft(
        uri = "content://test/document", mimeType = "application/pdf", extension = ".pdf",
        subject = "Test", description = "Dokument", date = 1_800_000_000_000L,
        tag = "Today", documentCategory = "WikiDiary", driveId = "file123",
    )

    @After
    fun close() {
        database.close()
        preferences.edit().clear().commit()
    }

    @Test
    fun restoredUploadCreatesNoteAndClearsPendingState() = runBlocking {
        store.setPending(draft)
        val viewModel = viewModel(200)
        withTimeout(5_000) { viewModel.status.first { !it.busy && it.pending != null } }
        viewModel.upload("test-token", null)
        withTimeout(5_000) { viewModel.status.first { it.saved && !it.busy } }
        assertEquals(1, database.wikiNoteDao().getNoteCount())
        assertNull(store.pending())
        assertTrue(config.getLastUpdated() > 0)
        viewModel.acknowledgeSaved()
        assertFalse(viewModel.status.value.saved)
    }

    @Test
    fun availableCategoriesComeFromApplicationRepository() = runBlocking {
        TestRepositories(database).categories.saveCategory(Category(0, "RealEstate"))
        val viewModel = viewModel(200)
        val categories = withTimeout(5_000) { viewModel.categories.first { it.isNotEmpty() } }
        assertEquals(listOf("RealEstate"), categories.map { it.name })
    }

    @Test
    fun missingFileDoesNotUploadOrSaveNote() = runBlocking {
        val viewModel = viewModel(200)
        withTimeout(5_000) { viewModel.status.first { !it.busy } }
        viewModel.upload("test-token", draft.copy(uri = ""))
        val status = withTimeout(5_000) { viewModel.status.first { !it.busy && it.error != null } }
        assertEquals("Wybierz plik z urządzenia.", status.error)
        assertEquals(0, database.wikiNoteDao().getNoteCount())
        assertNull(store.pending())
    }

    @Test
    fun failedAuthorizationKeepsPendingUploadAndDoesNotCreateNote() = runBlocking {
        store.setPending(draft)
        val viewModel = viewModel(401)
        withTimeout(5_000) { viewModel.status.first { !it.busy && it.pending != null } }
        viewModel.upload("test-token", null)
        withTimeout(5_000) { viewModel.status.first { !it.busy && it.error != null } }
        assertEquals(0, database.wikiNoteDao().getNoteCount())
        assertEquals(draft, store.pending())
        assertFalse(viewModel.status.value.saved)
    }

    private fun viewModel(code: Int): DocumentViewModel {
        val drive = DriveClient(context.contentResolver) { url ->
            object : HttpURLConnection(url) {
                override fun getResponseCode() = code
                override fun getInputStream() =
                    """{"id":"file123","name":"${draft.fileName}"}""".byteInputStream()
                override fun connect() = Unit
                override fun disconnect() = Unit
                override fun usingProxy() = false
            }
        }
        val repositories = TestRepositories(database)
        return DocumentViewModel(drive, store, repositories.tags, repositories.categories, config, SelectedDateStore())
    }
}
