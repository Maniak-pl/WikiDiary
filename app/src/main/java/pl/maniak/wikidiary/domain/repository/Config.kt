package pl.maniak.wikidiary.domain.repository

interface Config {
    fun isFirstLaunchToday(): Boolean
}