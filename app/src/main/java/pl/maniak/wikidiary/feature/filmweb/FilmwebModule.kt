package pl.maniak.wikidiary.feature.filmweb

import org.koin.dsl.module
import pl.maniak.wikidiary.feature.filmweb.ui.FilmwebViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.filmweb.domain.FilmPosterExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExportContributor
import org.koin.dsl.bind
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.filmweb.data.FilmRepositoryImpl
import pl.maniak.wikidiary.feature.filmweb.data.FilmwebSearchRepositoryImpl
import pl.maniak.wikidiary.feature.filmweb.domain.FilmRepository
import pl.maniak.wikidiary.feature.filmweb.domain.FilmwebSearchRepository

val filmwebModule = module {
    single { get<WikiNoteDatabase>().filmDao() }
    single { get<WikiNoteDatabase>().filmNoteDao() }
    single<FilmRepository> { FilmRepositoryImpl(get(), get(), get(), get(), get()) }
    single<FilmwebSearchRepository> { FilmwebSearchRepositoryImpl() }
    single {
        FilmPosterExportContributor(
            get<WikiNoteRepository>().observeNotes(),
            get<FilmRepository>().observeFilmNotes()
        )
    } bind WikiExportContributor::class
    viewModel { FilmwebViewModel(get(), get(), get(), get(), get()) }
}
