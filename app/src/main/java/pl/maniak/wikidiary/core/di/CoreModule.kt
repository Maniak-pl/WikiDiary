package pl.maniak.wikidiary.core.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.config.ConfigImpl
import pl.maniak.wikidiary.core.database.WikiNoteDatabase

val coreModule = module {
    single { androidContext().getSharedPreferences("config_prefs", Context.MODE_PRIVATE) }
    single<Config> { ConfigImpl(get()) }
    single { WikiNoteDatabase.getDatabase(androidContext()) }
}
