package pl.maniak.wikidiary.feature.export

import org.koin.dsl.module
import pl.maniak.wikidiary.feature.export.domain.WikiExporter

val exportModule = module {
    single { WikiExporter(getAll()) }
}
