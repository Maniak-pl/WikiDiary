package pl.maniak.wikidiary.di

import org.koin.dsl.module
import pl.maniak.wikidiary.core.di.coreModule
import pl.maniak.wikidiary.feature.diary.diaryModule
import pl.maniak.wikidiary.feature.export.exportModule
import pl.maniak.wikidiary.feature.books.booksModule
import pl.maniak.wikidiary.feature.expenses.expensesModule
import pl.maniak.wikidiary.feature.filmweb.filmwebModule
import pl.maniak.wikidiary.feature.people.peopleModule
import pl.maniak.wikidiary.feature.pictograms.pictogramsModule
import pl.maniak.wikidiary.feature.routines.routinesModule
import pl.maniak.wikidiary.feature.settings.settingsModule
import pl.maniak.wikidiary.feature.todo.todoModule
import pl.maniak.wikidiary.ui.widget.WikiWidgetProvider

val appModule = module {
    includes(
        coreModule,
        diaryModule,
        expensesModule,
        peopleModule,
        todoModule,
        routinesModule,
        pictogramsModule,
        filmwebModule,
        booksModule,
        exportModule,
        settingsModule
    )

    factory { WikiWidgetProvider() }
}
