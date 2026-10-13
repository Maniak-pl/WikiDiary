package pl.maniak.wikidiary.feature.routines

import org.koin.dsl.module
import pl.maniak.wikidiary.feature.routines.ui.RoutinesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.routines.data.RoutineMapper
import pl.maniak.wikidiary.feature.routines.data.RoutineRepositoryImpl
import pl.maniak.wikidiary.feature.routines.domain.RoutineRepository

val routinesModule = module {
    single { get<WikiNoteDatabase>().routineDao() }
    factory { RoutineMapper() }
    single<RoutineRepository> { RoutineRepositoryImpl(get(), get()) }
    viewModel { RoutinesViewModel(get(), get(), get(), get()) }
}
