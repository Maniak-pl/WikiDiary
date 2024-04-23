package pl.maniak.wikidiary.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.maniak.wikidiary.data.database.WikiNoteDatabase
import pl.maniak.wikidiary.data.mapper.CategoryMapper
import pl.maniak.wikidiary.data.mapper.TagMapper
import pl.maniak.wikidiary.data.mapper.TagMapperImpl
import pl.maniak.wikidiary.data.mapper.WikiNoteMapper
import pl.maniak.wikidiary.data.mapper.WikiNoteMapperImpl
import pl.maniak.wikidiary.domain.repository.CategoryRepository
import pl.maniak.wikidiary.domain.repository.CategoryRepositoryImpl
import pl.maniak.wikidiary.domain.repository.TagRepository
import pl.maniak.wikidiary.domain.repository.TagRepositoryImpl
import pl.maniak.wikidiary.domain.repository.WikiNoteRepository
import pl.maniak.wikidiary.domain.repository.WikiNoteRepositoryImpl
import pl.maniak.wikidiary.ui.MainViewModel

val appModule = module {

    // Database
    single { WikiNoteDatabase.getDatabase(androidContext()) }

    // Dao
    single { get<WikiNoteDatabase>().wikiNoteDao() }
    single { get<WikiNoteDatabase>().tagDao() }
    single { get<WikiNoteDatabase>().categoryDao() }

    // Repository
    single<WikiNoteRepository> { WikiNoteRepositoryImpl(get(), get()) }
    single<TagRepository> { TagRepositoryImpl(get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }

    // ViewModel
    viewModel { MainViewModel(get(), get(), get()) }

    // Mapper
    factory<WikiNoteMapper> { WikiNoteMapperImpl() }
    factory<TagMapper> { TagMapperImpl() }
    factory<CategoryMapper> { CategoryMapper() }
}