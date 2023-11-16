package pl.maniak.wikidiary.di

import org.koin.dsl.module
import pl.maniak.wikidiary.domain.WikiNoteRepository
import pl.maniak.wikidiary.domain.WikiNoteRepositoryImpl

val appModule = module {
    single<WikiNoteRepository> { WikiNoteRepositoryImpl() }
}