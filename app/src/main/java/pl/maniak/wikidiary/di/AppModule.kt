package pl.maniak.wikidiary.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.maniak.wikidiary.core.di.coreModule
import pl.maniak.wikidiary.feature.diary.diaryModule
import pl.maniak.wikidiary.feature.filmweb.filmwebModule
import pl.maniak.wikidiary.feature.people.peopleModule
import pl.maniak.wikidiary.feature.pictograms.pictogramsModule
import pl.maniak.wikidiary.feature.routines.routinesModule
import pl.maniak.wikidiary.feature.todo.todoModule
import pl.maniak.wikidiary.ui.MainViewModel
import pl.maniak.wikidiary.ui.widget.WikiWidgetProvider

val appModule = module {
    includes(
        coreModule,
        diaryModule,
        peopleModule,
        todoModule,
        routinesModule,
        pictogramsModule,
        filmwebModule
    )

    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }

    factory { WikiWidgetProvider() }
}
