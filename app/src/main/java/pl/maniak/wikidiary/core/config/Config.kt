package pl.maniak.wikidiary.core.config

interface Config {
    fun isFirstLaunchToday(): Boolean
    fun getLastUpdated(): Long
    fun setLastUpdated(time: Long)
    fun getWikiPageUrl(): String
    fun setWikiPageUrl(url: String)
}