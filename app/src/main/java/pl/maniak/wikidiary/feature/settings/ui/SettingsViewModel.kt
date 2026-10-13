package pl.maniak.wikidiary.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.maniak.wikidiary.core.config.Config

data class SettingsUiState(
    val darkTheme: Boolean? = null,
    val wikiPageUrl: String = "",
)

sealed interface SettingsIntent {
    data class SetDarkTheme(val enabled: Boolean) : SettingsIntent
    data class SaveWikiPageUrl(val url: String) : SettingsIntent
}

class SettingsViewModel(private val config: Config) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        config.darkTheme,
        config.wikiPageUrl,
        ::SettingsUiState
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(config.darkTheme.value, config.wikiPageUrl.value)
    )

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetDarkTheme -> config.setDarkTheme(intent.enabled)
            is SettingsIntent.SaveWikiPageUrl -> config.setWikiPageUrl(intent.url)
        }
    }
}
