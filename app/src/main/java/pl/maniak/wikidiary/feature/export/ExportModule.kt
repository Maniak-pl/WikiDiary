package pl.maniak.wikidiary.feature.export

import org.koin.dsl.module
import pl.maniak.wikidiary.feature.export.ui.ExportViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import pl.maniak.wikidiary.feature.export.domain.WikiExporter

val exportModule = module {
    single { WikiExporter(getAll()) }
    viewModel { ExportViewModel(get(), get()) }
}
