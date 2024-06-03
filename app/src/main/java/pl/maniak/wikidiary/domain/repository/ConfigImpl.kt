package pl.maniak.wikidiary.domain.repository

import android.content.SharedPreferences
import java.util.Calendar

class ConfigImpl(private val sharedPreferences: SharedPreferences) :Config {
    private val lastLaunchDayKey = "last_launch_day"

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
}