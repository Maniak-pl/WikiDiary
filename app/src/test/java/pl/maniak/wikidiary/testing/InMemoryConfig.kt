package pl.maniak.wikidiary.testing

import kotlinx.coroutines.flow.MutableStateFlow
import pl.maniak.wikidiary.core.config.Config

class InMemoryConfig(
    private val firstLaunchToday: Boolean = false,
    wikiPageUrl: String = "",
) : Config {
    private var lastUpdated = 0L

    override fun isFirstLaunchToday() = firstLaunchToday
    override fun getLastUpdated() = lastUpdated
    override fun setLastUpdated(time: Long) {
        lastUpdated = time
    }

    override val wikiPageUrl = MutableStateFlow(wikiPageUrl)
    override fun setWikiPageUrl(url: String) {
        wikiPageUrl.value = url.trim()
    }

    override val darkTheme = MutableStateFlow<Boolean?>(null)
    override fun setDarkTheme(enabled: Boolean) {
        darkTheme.value = enabled
    }
}
