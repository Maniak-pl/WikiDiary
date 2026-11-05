package pl.maniak.wikidiary.feature.backup

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BackupState(
    val account: String = "",
    val enabled: Boolean = false,
    val restoreAvailable: Boolean = false,
    val lastSaved: Long = 0,
    val hash: String = "",
    val attemptedHash: String = "",
    val dirty: Boolean = false,
    val error: String? = null,
)

class BackupPreferences(private val preferences: SharedPreferences) {
    private val mutableState = MutableStateFlow(read())
    val state = mutableState.asStateFlow()

    @Synchronized
    fun update(transform: (BackupState) -> BackupState) {
        val next = transform(mutableState.value)
        check(preferences.edit()
            .putString("account", next.account)
            .putBoolean("enabled", next.enabled)
            .putBoolean("restore_available", next.restoreAvailable)
            .putLong("last_saved", next.lastSaved)
            .putString("hash", next.hash)
            .putString("attempted_hash", next.attemptedHash)
            .putBoolean("dirty", next.dirty)
            .putString("error", next.error)
            .commit()
        ) { "Nie udało się zapisać ustawień kopii." }
        mutableState.value = next
    }

    private fun read() = BackupState(
        account = preferences.getString("account", "").orEmpty(),
        enabled = preferences.getBoolean("enabled", false),
        restoreAvailable = preferences.getBoolean("restore_available", false),
        lastSaved = preferences.getLong("last_saved", 0),
        hash = preferences.getString("hash", "").orEmpty(),
        attemptedHash = preferences.getString("attempted_hash", "").orEmpty(),
        dirty = preferences.getBoolean("dirty", false),
        error = preferences.getString("error", null),
    )
}
