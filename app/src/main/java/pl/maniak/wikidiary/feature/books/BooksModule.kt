package pl.maniak.wikidiary.feature.books

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.maniak.wikidiary.feature.books.data.BookRepositoryImpl
import pl.maniak.wikidiary.feature.books.data.LubimyczytacSearchRepository
import pl.maniak.wikidiary.feature.books.domain.BookCoverExportContributor
import pl.maniak.wikidiary.feature.books.domain.BookRepository
import pl.maniak.wikidiary.feature.books.domain.BookSearchRepository
import pl.maniak.wikidiary.feature.books.ui.BooksViewModel
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor

val booksModule = module {
    single<BookSearchRepository> { LubimyczytacSearchRepository() }
    single<BookRepository> { BookRepositoryImpl(get(), get()) }
    single {
        BookCoverExportContributor(
            get<WikiNoteRepository>().observeNotes(),
            get<BookRepository>().observeBookNotes()
        )
    } bind WikiExportContributor::class
    viewModel { BooksViewModel(get(), get(), get(), get(), get()) }
}
