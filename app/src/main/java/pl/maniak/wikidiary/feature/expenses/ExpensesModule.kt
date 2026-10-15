package pl.maniak.wikidiary.feature.expenses

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.maniak.wikidiary.feature.expenses.data.ExpenseRepositoryImpl
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseRepository
import pl.maniak.wikidiary.feature.expenses.ui.ExpenseViewModel

val expensesModule = module {
    single<ExpenseRepository> { ExpenseRepositoryImpl(get(), get()) }
    viewModel { ExpenseViewModel(get(), get(), get(), get()) }
}
