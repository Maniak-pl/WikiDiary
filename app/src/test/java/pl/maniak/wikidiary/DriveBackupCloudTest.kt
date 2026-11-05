package pl.maniak.wikidiary

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.backup.*
import pl.maniak.wikidiary.testing.inMemoryDatabase
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class DriveBackupCloudTest {
    private val database = inMemoryDatabase()
    private val file get() = runBlocking { BackupFile(100, ConfigurationStore(database).snapshot()) }

    @After fun close() = database.close()

    @Test fun lookupUsesHiddenAppDataFolderAndReadsLatestGeneration() {
        val requests = mutableListOf<Response>()
        val client = DriveBackupCloud { url ->
            Response(url, 200, if (url.query.contains("alt=media")) file.encode()
                else """{"files":[{"id":"backup123"}]}""").also { requests += it }
        }
        val remote = requireNotNull(client.read("access-token"))
        assertTrue(requests.first().url.query.contains("spaces=appDataFolder"))
        assertEquals("backup123", remote.id)
        assertTrue(requests.first().url.query.contains("orderBy=createdTime%20desc"))
        assertEquals(file, remote.file)
        assertTrue(requests.all { it.getRequestProperty("Authorization") == "Bearer access-token" })
    }

    @Test fun createWritesOnlyConfigurationIntoHiddenFolder() {
        lateinit var request: Response
        val client = DriveBackupCloud { url -> Response(url, 200, """{"id":"new-backup"}""").also { request = it } }
        client.write("token", file)
        val body = request.sent.toString("UTF-8")
        assertEquals("POST", request.requestMethod)
        assertTrue(body.contains("\"parents\":[\"appDataFolder\"]"))
        assertTrue(body.contains(DriveBackupCloud.FILE_NAME))
        assertTrue(body.contains(file.encode()))
        assertFalse(body.contains("wiki_note_table"))
    }

    @Test fun corruptLatestGenerationDoesNotSilentlyRestoreOlderOrEmptyData() {
        val corrupt = DriveBackupCloud { url ->
            Response(url, 200, if (url.query.contains("alt=media")) "{}"
                else """{"files":[{"id":"latest"}]}""")
        }
        assertThrows(kotlinx.serialization.SerializationException::class.java) { corrupt.read("token") }
    }

    @Test fun temporaryGoogleErrorIsRetryableButPermissionErrorNeedsUser() {
        val temporary = DriveBackupCloud { url -> Response(url, 503, "{}") }
        assertTrue(assertThrows(BackupCloudException::class.java) { temporary.read("token") }.retryable)
        val denied = DriveBackupCloud { url -> Response(url, 403, "{}") }
        assertFalse(assertThrows(BackupCloudException::class.java) { denied.read("token") }.retryable)
    }

    private class Response(
        url: URL, private val code: Int, private val body: String,
    ) : HttpURLConnection(url) {
        val sent = ByteArrayOutputStream()
        override fun getResponseCode() = code
        override fun getInputStream() = body.byteInputStream()
        override fun getOutputStream() = sent
        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy() = false
    }
}
