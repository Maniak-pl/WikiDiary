package pl.maniak.wikidiary.core.config

import android.content.SharedPreferences
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigImpl(private val sharedPreferences: SharedPreferences) : Config {
    private val lastLaunchDayKey = "last_launch_day"
    private val lastUpdatedKey = "last_updated"
    private val wikiPageUrlKey = "wiki_page_url"
    private val darkThemeKey = "dark_theme"

    private val wikiPageUrlState =
        MutableStateFlow(sharedPreferences.getString(wikiPageUrlKey, "").orEmpty())
    private val darkThemeState = MutableStateFlow(
        if (sharedPreferences.contains(darkThemeKey)) sharedPreferences.getBoolean(darkThemeKey, false) else null
    )

    override val wikiPageUrl: StateFlow<String> = wikiPageUrlState.asStateFlow()
    override val darkTheme: StateFlow<Boolean?> = darkThemeState.asStateFlow()

    override fun isFirstLaunchToday(): Boolean {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastLaunchDay = sharedPreferences.getInt(lastLaunchDayKey, -1)
        return if (currentDay != lastLaunchDay) {
            sharedPreferences.edit().putInt(lastLaunchDayKey, currentDay).apply()
            true
        } else {
            false
        }
    }

    override fun getLastUpdated(): Long {
        return sharedPreferences.getLong(lastUpdatedKey, 0)
    }

    override fun setLastUpdated(time: Long) {
        sharedPreferences.edit().putLong(lastUpdatedKey, time).apply()
    }

    override fun setWikiPageUrl(url: String) {
        val normalizedUrl = url.trim()
        sharedPreferences.edit().putString(wikiPageUrlKey, normalizedUrl).apply()
        wikiPageUrlState.value = normalizedUrl
    }

    override fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(darkThemeKey, enabled).apply()
        darkThemeState.value = enabled
    }
}