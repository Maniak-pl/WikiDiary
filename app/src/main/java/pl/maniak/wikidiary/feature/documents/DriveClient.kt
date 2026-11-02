package pl.maniak.wikidiary.feature.documents

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class DriveClient(
    private val resolver: ContentResolver,
    private val openConnection: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
) {
    suspend fun generateId(token: String): String = withContext(Dispatchers.IO) {
        request(token, "GET", "/files/generateIds?count=1&space=drive&type=files")
            .getJSONArray("ids").getString(0)
            .also { require(it.matches(Regex("[A-Za-z0-9_-]+"))) { "Niepoprawny identyfikator pliku z Drive." } }
    }

    suspend fun upload(token: String, draft: DocumentDraft) = withContext(Dispatchers.IO) {
        // Reuse a generated ID after an interrupted upload instead of creating another file.
        val existing = request(
            token, "GET", "/files/${draft.driveId}?fields=id,name,trashed", allowMissing = true
        )
        if (existing.has("id")) {
            require(!existing.optBoolean("trashed") && existing.getString("name") == draft.fileName
            ) { "Plik na Drive został zmieniony. Sprawdź go przed ponowieniem zapisu." }
            return@withContext
        }
        val boundary = "wikidiary-${UUID.randomUUID()}"
        val metadata = JSONObject()
            .put("id", draft.driveId)
            .put("name", draft.fileName)
        val connection = connection(
            token, "POST", "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id"
        )
        try {
            connection.doOutput = true
            connection.setChunkedStreamingMode(64 * 1024)
            connection.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
            resolver.openInputStream(Uri.parse(draft.uri)).use { input ->
                requireNotNull(input) { "Nie można odczytać wybranego pliku." }
                connection.outputStream.use { output ->
                    output.write(("--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n" +
                        "$metadata\r\n--$boundary\r\nContent-Type: ${draft.mimeType}\r\n\r\n").toByteArray())
                    input.copyTo(output)
                    output.write("\r\n--$boundary--\r\n".toByteArray())
                }
            }
            val response = response(connection)
            check(response.getString("id") == draft.driveId) { "Drive zwrócił inny identyfikator pliku." }
        } finally {
            connection.disconnect()
        }
    }

    private fun request(
        token: String,
        method: String,
        path: String,
        allowMissing: Boolean = false,
    ): JSONObject {
        val connection = connection(token, method, "https://www.googleapis.com/drive/v3$path")
        return try {
            if (allowMissing && connection.responseCode == 404) JSONObject() else response(connection)
        } finally {
            connection.disconnect()
        }
    }

    private fun connection(token: String, method: String, url: String): HttpURLConnection =
        openConnection(URL(url)).apply {
            requestMethod = method
            connectTimeout = 30_000
            readTimeout = 120_000
            instanceFollowRedirects = false
            setRequestProperty("Authorization", "Bearer $token")
        }

    private fun response(connection: HttpURLConnection): JSONObject {
        val code = connection.responseCode
        if (code !in 200..299) {
            val message = when (code) {
                401 -> "Autoryzacja Google wygasła. Ponów i zatwierdź dostęp."
                403 -> "Brak uprawnień lub miejsca na Drive. Sprawdź dostęp i konfigurację Google Cloud."
                404 -> "Plik nie jest dostępny dla wybranego konta Google."
                else -> "Google Drive: błąd HTTP $code. Ponów operację."
            }
            throw IOException(message)
        }
        return connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
    }
}
