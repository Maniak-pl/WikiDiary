package pl.maniak.wikidiary.feature.export.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.feature.export.domain.WikiExporter

data class ExportUiState(
    val document: String = "",
    val wikiPageUrl: String = "",
)

sealed interface ExportIntent {
    data class SaveWikiPageUrl(val url: String) : ExportIntent
}

class ExportViewModel(
    wikiExporter: WikiExporter,
    private val config: Config,
) : ViewModel() {

    private val wikiPageUrl = MutableStateFlow(config.getWikiPageUrl())

    val uiState: StateFlow<ExportUiState> = combine(
        wikiExporter.document(),
        wikiPageUrl,
        ::ExportUiState
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ExportUiState(wikiPageUrl = wikiPageUrl.value)
    )

    fun onIntent(intent: ExportIntent) {
        when (intent) {
            is ExportIntent.SaveWikiPageUrl -> {
                val url = intent.url.trim()
                config.setWikiPageUrl(url)
                wikiPageUrl.value = url
            }
        }
    }
}
