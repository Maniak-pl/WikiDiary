package pl.maniak.wikidiary.testing

import pl.maniak.wikidiary.core.config.Config

class InMemoryConfig(
    private val firstLaunchToday: Boolean = false,
    private var wikiPageUrl: String = "",
) : Config {
    private var lastUpdated = 0L

    override fun isFirstLaunchToday() = firstLaunchToday
    override fun getLastUpdated() = lastUpdated
    override fun setLastUpdated(time: Long) {
        lastUpdated = time
    }
    override fun getWikiPageUrl() = wikiPageUrl
    override fun setWikiPageUrl(url: String) {
        wikiPageUrl = url
    }
}
