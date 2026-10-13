package pl.maniak.wikidiary.core.config

import kotlinx.coroutines.flow.StateFlow

interface Config {
    fun isFirstLaunchToday(): Boolean
    fun getLastUpdated(): Long
    fun setLastUpdated(time: Long)

    /** Saved Wiki page address; empty when not set. */
    val wikiPageUrl: StateFlow<String>
    fun setWikiPageUrl(url: String)

    /** Theme chosen in settings; `null` follows the system setting. */
    val darkTheme: StateFlow<Boolean?>
    fun setDarkTheme(enabled: Boolean)
}
