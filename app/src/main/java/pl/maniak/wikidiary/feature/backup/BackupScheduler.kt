package pl.maniak.wikidiary.feature.backup

import android.content.Context
import android.util.Log
import androidx.room.InvalidationTracker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackupScheduler(
    context: Context,
    private val database: WikiNoteDatabase,
    private val preferences: BackupPreferences,
) {
    private val work = WorkManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            database.invalidationTracker.addObserver(object : InvalidationTracker.Observer(
                "tag_table", "categories", "todo_table", "routines", "pictogram_table", "person_table"
            ) {
                override fun onInvalidated(tables: Set<String>) {
                    if (preferences.state.value.enabled) {
                        preferences.update { it.copy(dirty = true) }
                        enqueue()
                    }
                }
            })
            if (preferences.state.value.enabled) enqueue()
        }
        work.enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ConfigurationBackupWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints()).build())
    }

    fun enqueue() {
        work.enqueueUniqueWork(ONCE, ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ConfigurationBackupWorker>()
                .setInitialDelay(3, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .setConstraints(constraints()).build())
    }

    private fun constraints() = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    companion object {
        private const val ONCE = "configuration-backup"
        private const val PERIODIC = "configuration-backup-periodic"
    }
}

class ConfigurationBackupWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {
    private val coordinator: BackupCoordinator by inject()
    private val preferences: BackupPreferences by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!preferences.state.value.enabled) return@withContext Result.success()
        try {
            if (coordinator.save()) Result.success() else Result.retry()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e("ConfigurationBackup", "Automatic backup failed", exception)
            preferences.update { it.copy(error = exception.message ?: "Nie udało się zapisać kopii.", dirty = true) }
            when (exception) {
                is BackupCloudException -> if (exception.retryable) Result.retry() else Result.failure()
                is IOException -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}
