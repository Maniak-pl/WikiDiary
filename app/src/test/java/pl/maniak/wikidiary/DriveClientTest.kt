package pl.maniak.wikidiary

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.documents.DocumentDraft
import pl.maniak.wikidiary.feature.documents.DriveClient
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class DriveClientTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val draft = DocumentDraft(
        uri = "", mimeType = "application/pdf", extension = ".pdf", subject = "Test",
        description = "Dokument", date = 1_800_000_000_000L, tag = "Today",
        documentCategory = "WikiDiary",
        driveId = "file123",
    )

    @Test
    fun retryUsesExistingDriveFileWithoutUploadingAgain() = runBlocking {
        var calls = 0
        val client = DriveClient(context.contentResolver) { url ->
            calls++
            Response(url, 200, """{"id":"file123","name":"${draft.fileName}"}""")
        }
        client.upload("test-token", draft)
        assertEquals(1, calls)
    }

    @Test
    fun uploadStreamsSelectedFileToDriveRootWithoutParent() = runBlocking {
        val file = java.io.File(context.cacheDir, "drive-test-document.pdf")
        try {
            file.writeText("test document bytes")
            val uploaded = Response(URL("https://www.googleapis.com"), 200, """{"id":"file123"}""")
            val client = DriveClient(context.contentResolver) { url ->
                if (url.path.contains("/upload/")) uploaded else Response(url, 404, "{}")
            }
            client.upload("test-token", draft.copy(uri = Uri.fromFile(file).toString()))
            val multipart = uploaded.sent.toString("UTF-8")
            assertTrue(multipart.contains(draft.fileName))
            assertFalse(multipart.contains("\"parents\""))
            assertTrue(multipart.contains("test document bytes"))
            assertTrue(multipart.contains("\"id\":\"file123\""))
        } finally {
            file.delete()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun changedExistingFileDoesNotSaveAnIncorrectLink() = runBlocking {
        val client = DriveClient(context.contentResolver) { url ->
            Response(url, 200, """{"id":"file123","name":"Renamed.pdf"}""")
        }
        client.upload("test-token", draft)
    }

    @Test(expected = IOException::class)
    fun expiredAuthorizationFailsInsteadOfCreatingAnotherFile() = runBlocking {
        val client = DriveClient(context.contentResolver) { url -> Response(url, 401, "{}") }
        client.upload("test-token", draft)
    }

    private class Response(url: URL, private val code: Int, private val body: String) : HttpURLConnection(url) {
        val sent = ByteArrayOutputStream()
        override fun getResponseCode() = code
        override fun getInputStream() = ByteArrayInputStream(body.toByteArray())
        override fun getOutputStream() = sent
        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy() = false
    }
}
