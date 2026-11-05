package pl.maniak.wikidiary

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import pl.maniak.wikidiary.di.appModule
import org.koin.core.context.GlobalContext.get
import pl.maniak.wikidiary.feature.backup.BackupScheduler

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
        get().get<BackupScheduler>().start()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}