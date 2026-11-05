package pl.maniak.wikidiary.feature.backup

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class RemoteBackup(val id: String, val file: BackupFile)

interface BackupCloud {
    fun read(token: String): RemoteBackup?
    fun write(token: String, file: BackupFile)
}

class BackupCloudException(message: String, val retryable: Boolean) : IOException(message)

class DriveBackupCloud(
    private val openConnection: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
) : BackupCloud {
    override fun read(token: String): RemoteBackup? {
        val query = java.net.URLEncoder.encode(
            "name = '$FILE_NAME' and trashed = false and " +
                "appProperties has { key='wikidiary_backup' and value='configuration-v1' }", "UTF-8")
        val listing = JSONObject(request(token,
            "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=$query&fields=files(id)&orderBy=createdTime%20desc&pageSize=1"
        ))
        val files = listing.getJSONArray("files")
        if (files.length() == 0) return null
        val id = files.getJSONObject(0).getString("id")
        require(id.matches(Regex("[A-Za-z0-9_-]+"))) { "Niepoprawny identyfikator kopii." }
        val body = request(token, "https://www.googleapis.com/drive/v3/files/$id?alt=media")
        return RemoteBackup(id, BackupFile.decode(body))
    }

    override fun write(token: String, file: BackupFile) {
        val content = file.encode()
        require(content.toByteArray(Charsets.UTF_8).size <= BackupFile.MAX_BYTES) { "Kopia jest zbyt duża." }
        val boundary = "wikidiary-backup-${java.util.UUID.randomUUID()}"
        val metadata = JSONObject().put("name", FILE_NAME)
            .put("parents", org.json.JSONArray().put("appDataFolder"))
            .put("appProperties", JSONObject().put("wikidiary_backup", "configuration-v1"))
        val body = "--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n" +
            "$metadata\r\n--$boundary\r\nContent-Type: application/json\r\n\r\n" +
            "$content\r\n--$boundary--\r\n"
        // A new generation never overwrites the last valid backup when an upload is interrupted.
        val response = JSONObject(request(token,
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id",
            method = "POST", body = body, contentType = "multipart/related; boundary=$boundary"))
        require(response.getString("id").matches(Regex("[A-Za-z0-9_-]+"))) {
            "Google nie potwierdził identyfikatora kopii."
        }
    }

    private fun request(
        token: String,
        url: String,
        method: String = "GET",
        body: String? = null,
        contentType: String? = null,
    ): String {
        val connection = openConnection(URL(url))
        try {
            connection.requestMethod = method
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", contentType)
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            if (code !in 200..299) {
                val message = when (code) {
                    401 -> "Dostęp do kopii wygasł. Zatwierdź dostęp w ustawieniach."
                    403 -> "Google odrzucił dostęp do kopii. Sprawdź zgodę drive.appdata i miejsce na Drive."
                    404 -> "Kopia nie jest już dostępna. Połącz konto ponownie."
                    else -> "Nie udało się zapisać kopii Google (HTTP $code)."
                }
                throw BackupCloudException(message, code == 429 || code >= 500)
            }
            val bytes = connection.inputStream.use { it.readBytesLimited(BackupFile.MAX_BYTES) }
            return bytes.toString(Charsets.UTF_8)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val FILE_NAME = "wikidiary-configuration-v1.json"
    }
}

private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        require(output.size() + count <= limit) { "Kopia jest zbyt duża." }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}
