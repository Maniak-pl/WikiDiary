package pl.maniak.wikidiary.feature.backup

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BackupCoordinator(
    private val store: ConfigurationStore,
    private val cloud: BackupCloud,
    private val preferences: BackupPreferences,
    private val token: suspend (String) -> String,
    private val hasPendingDocument: () -> Boolean,
) {
    private val mutex = Mutex()

    suspend fun connect(account: String, accessToken: String) = mutex.withLock {
        require(account.isNotBlank()) { "Wybierz konto Google." }
        val remote = cloud.read(accessToken)
        if (remote != null) {
            val current = preferences.state.value
            val remoteHash = remote.file.data.hash()
            val knownCopy = current.account == account &&
                (!current.restoreAvailable || (current.attemptedHash.isNotBlank() && remoteHash == current.attemptedHash)) &&
                (remoteHash == current.hash || remoteHash == current.attemptedHash)
            val dirty = knownCopy && store.snapshot().hash() != remoteHash
            preferences.update { BackupState(account = account, enabled = knownCopy,
                restoreAvailable = !knownCopy, lastSaved = remote.file.savedAt, hash = remoteHash,
                dirty = dirty) }
        } else {
            // Persist the account before uploading, so an interrupted first upload is never overwritten blindly.
            preferences.update { BackupState(account = account, dirty = true) }
            writeLocalSnapshot(accessToken)
        }
    }

    suspend fun save(accessToken: String? = null): Boolean = mutex.withLock {
        val state = preferences.state.value
        if (!state.enabled) return@withLock true
        val snapshot = store.snapshot()
        val hash = snapshot.hash()
        val access = accessToken ?: token(state.account)
        val remote = requireNotNull(cloud.read(access)) {
            "Nie znaleziono wcześniejszej kopii. Zapis wstrzymano. Połącz konto ponownie."
        }
        val remoteHash = remote.file.data.hash()
        require(remoteHash == state.hash || remoteHash == state.attemptedHash || remoteHash == hash) {
            "Kopia Google zawiera inne dane. Zapis wstrzymano. Połącz konto ponownie i sprawdź kopię."
        }
        val savedAt = if (remoteHash != hash) {
            preferences.update { it.copy(attemptedHash = hash) }
            val file = BackupFile(System.currentTimeMillis(), snapshot)
            cloud.write(access, file)
            file.savedAt
        } else remote.file.savedAt
        val dirty = store.snapshot().hash() != hash
        preferences.update { it.copy(lastSaved = savedAt, hash = hash, attemptedHash = "",
            dirty = dirty, error = null) }
        !dirty
    }

    suspend fun restore(accessToken: String) = mutex.withLock {
        require(preferences.state.value.account.isNotBlank()) { "Najpierw połącz konto." }
        require(!hasPendingDocument()) { "Najpierw dokończ lub anuluj przesyłanie dokumentu." }
        val remote = requireNotNull(cloud.read(accessToken)) { "Nie znaleziono kopii na tym koncie Google." }
        preferences.update { it.copy(enabled = false, restoreAvailable = true) }
        store.restore(remote.file.data)
        val dirty = store.snapshot().hash() != remote.file.data.hash()
        preferences.update { it.copy(enabled = true, restoreAvailable = false,
            lastSaved = remote.file.savedAt, hash = remote.file.data.hash(),
            attemptedHash = "", dirty = dirty, error = null) }
    }

    suspend fun saveLocalCopy(accessToken: String) = mutex.withLock {
        require(preferences.state.value.account.isNotBlank()) { "Najpierw połącz konto." }
        writeLocalSnapshot(accessToken)
    }

    suspend fun disconnect() = mutex.withLock {
        preferences.update { it.copy(enabled = false, dirty = false, error = null) }
    }

    private suspend fun writeLocalSnapshot(accessToken: String) {
        val snapshot = store.snapshot()
        preferences.update { it.copy(attemptedHash = snapshot.hash()) }
        val file = BackupFile(System.currentTimeMillis(), snapshot)
        cloud.write(accessToken, file)
        val dirty = store.snapshot().hash() != snapshot.hash()
        preferences.update { it.copy(enabled = true, restoreAvailable = false, lastSaved = file.savedAt,
            hash = snapshot.hash(), attemptedHash = "", dirty = dirty, error = null) }
    }
}
