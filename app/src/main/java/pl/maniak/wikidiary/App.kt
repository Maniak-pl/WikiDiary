package pl.maniak.wikidiary

import android.app.Application
import org.koin.core.context.GlobalContext.startKoin
import pl.maniak.wikidiary.di.appModule

class App: Application() {

        override fun onCreate() {
            super.onCreate()
            initKoin()
        }

    private fun initKoin() {
        startKoin {
            modules(appModule)
        }
    }
}