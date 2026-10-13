package pl.maniak.wikidiary.feature.todo

import org.koin.dsl.module
import pl.maniak.wikidiary.feature.todo.ui.TodoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.todo.data.TodoMapper
import pl.maniak.wikidiary.feature.todo.data.TodoRepositoryImpl
import pl.maniak.wikidiary.feature.todo.domain.AddTodo
import pl.maniak.wikidiary.feature.todo.domain.TodoRepository

val todoModule = module {
    single { get<WikiNoteDatabase>().todoDao() }
    factory { TodoMapper() }
    single<TodoRepository> { TodoRepositoryImpl(get(), get()) }
    factory { AddTodo(get(), get(), get()) }
    viewModel { TodoViewModel(get(), get(), get(), get()) }
}
