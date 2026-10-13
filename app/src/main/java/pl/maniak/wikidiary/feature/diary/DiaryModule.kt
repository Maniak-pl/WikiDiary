package pl.maniak.wikidiary.feature.diary

import org.koin.dsl.module
import pl.maniak.wikidiary.feature.diary.ui.NotesListViewModel
import pl.maniak.wikidiary.feature.diary.ui.TagEditorViewModel
import pl.maniak.wikidiary.feature.diary.ui.DiaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import pl.maniak.wikidiary.feature.diary.domain.NotesExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import org.koin.dsl.bind
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.diary.data.CategoryMapper
import pl.maniak.wikidiary.feature.diary.data.CategoryRepositoryImpl
import pl.maniak.wikidiary.feature.diary.data.TagMapper
import pl.maniak.wikidiary.feature.diary.data.TagMapperImpl
import pl.maniak.wikidiary.feature.diary.data.TagRepositoryImpl
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapper
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapperImpl
import pl.maniak.wikidiary.feature.diary.data.WikiNoteRepositoryImpl
import pl.maniak.wikidiary.feature.diary.domain.CategoryRepository
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository

val diaryModule = module {
    single { get<WikiNoteDatabase>().wikiNoteDao() }
    single { get<WikiNoteDatabase>().tagDao() }
    single { get<WikiNoteDatabase>().categoryDao() }
    single { get<WikiNoteDatabase>().notePersonDao() }
    factory<WikiNoteMapper> { WikiNoteMapperImpl() }
    factory<TagMapper> { TagMapperImpl() }
    factory { CategoryMapper() }
    single<WikiNoteRepository> { WikiNoteRepositoryImpl(get(), get(), get(), get(), get()) }
    single<TagRepository> { TagRepositoryImpl(get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }
    single { NotesExportContributor(get<WikiNoteRepository>().observeNotes()) } bind WikiExportContributor::class
    viewModel { DiaryViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { TagEditorViewModel(get(), get()) }
    viewModel { NotesListViewModel(get(), get()) }
}
