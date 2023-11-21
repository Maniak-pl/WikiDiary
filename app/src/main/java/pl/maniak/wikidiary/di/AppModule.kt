package pl.maniak.wikidiary.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.maniak.wikidiary.data.database.WikiNoteDatabase
import pl.maniak.wikidiary.data.mapper.WikiNoteMapper
import pl.maniak.wikidiary.data.mapper.WikiNoteMapperImpl
import pl.maniak.wikidiary.domain.repository.WikiNoteRepository
import pl.maniak.wikidiary.domain.repository.WikiNoteRepositoryImpl
import pl.maniak.wikidiary.ui.MainViewModel

val appModule = module {

    // Database
    single { WikiNoteDatabase.getDatabase(androidContext()) }

    // Dao
    single { get<WikiNoteDatabase>().wikiNoteDao() }

    // Repository
    single<WikiNoteRepository> { WikiNoteRepositoryImpl(get(), get()) }

    // ViewModel
    viewModel { MainViewModel(get()) }

    // Mapper
    factory<WikiNoteMapper> { WikiNoteMapperImpl() }
}