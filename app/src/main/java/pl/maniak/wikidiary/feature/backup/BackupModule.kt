package pl.maniak.wikidiary.feature.backup

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.maniak.wikidiary.feature.documents.DocumentStore

val backupModule = module {
    single { BackupPreferences(androidContext().getSharedPreferences("google_backup", Context.MODE_PRIVATE)) }
    single { ConfigurationStore(get()) }
    single<BackupCloud> { DriveBackupCloud() }
    single { BackupAuthorization(androidContext()) }
    single {
        val authorization: BackupAuthorization = get()
        val documents: DocumentStore = get()
        BackupCoordinator(get(), get(), get(), authorization::token, { documents.pending() != null })
    }
    single { BackupScheduler(androidContext(), get(), get()) }
    viewModel { BackupViewModel(get(), get(), get()) }
}
