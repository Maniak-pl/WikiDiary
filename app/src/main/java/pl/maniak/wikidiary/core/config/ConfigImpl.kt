package pl.maniak.wikidiary.core.config

import android.content.SharedPreferences
import java.util.Calendar

class ConfigImpl(private val sharedPreferences: SharedPreferences) : Config {
    private val lastLaunchDayKey = "last_launch_day"
    private val lastUpdatedKey = "last_updated"
    private val wikiPageUrlKey = "wiki_page_url"

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

    override fun getWikiPageUrl(): String {
        return sharedPreferences.getString(wikiPageUrlKey, "").orEmpty()
    }

    override fun setWikiPageUrl(url: String) {
        sharedPreferences.edit().putString(wikiPageUrlKey, url).apply()
    }
}