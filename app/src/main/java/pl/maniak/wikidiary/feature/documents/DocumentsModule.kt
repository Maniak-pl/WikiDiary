package pl.maniak.wikidiary.feature.documents

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val documentsModule = module {
    single { DriveClient(androidContext().contentResolver) }
    single { DocumentStore(get(), get()) }
    viewModel { DocumentViewModel(get(), get(), get(), get(), get(), get()) }
}
