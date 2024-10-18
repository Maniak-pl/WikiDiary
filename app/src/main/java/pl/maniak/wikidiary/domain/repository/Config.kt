package pl.maniak.wikidiary.domain.repository

interface Config {
    fun isFirstLaunchToday(): Boolean
    fun getLastUpdated(): Long
    fun setLastUpdated(time: Long)
}