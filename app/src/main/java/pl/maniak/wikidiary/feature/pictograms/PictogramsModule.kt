package pl.maniak.wikidiary.feature.pictograms

import org.koin.dsl.module
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import org.koin.dsl.bind
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.pictograms.data.PictogramEntryMapper
import pl.maniak.wikidiary.feature.pictograms.data.PictogramMapper
import pl.maniak.wikidiary.feature.pictograms.data.PictogramRepositoryImpl
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramRepository

val pictogramsModule = module {
    single { get<WikiNoteDatabase>().pictogramDao() }
    single { get<WikiNoteDatabase>().pictogramEntryDao() }
    factory { PictogramMapper() }
    factory { PictogramEntryMapper() }
    single<PictogramRepository> { PictogramRepositoryImpl(get(), get(), get(), get()) }
    single {
        PictogramExportContributor(get<PictogramRepository>().observePictogramEntries())
    } bind WikiExportContributor::class
}
