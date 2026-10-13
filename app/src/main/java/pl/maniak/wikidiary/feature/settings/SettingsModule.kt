package pl.maniak.wikidiary.feature.settings

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.maniak.wikidiary.feature.settings.ui.SettingsViewModel

val settingsModule = module {
    viewModel { SettingsViewModel(get()) }
}
