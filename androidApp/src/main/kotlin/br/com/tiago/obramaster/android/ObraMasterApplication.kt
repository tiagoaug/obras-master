package br.com.tiago.obramaster.android

import android.app.Application
import br.com.tiago.obramaster.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ObraMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ObraMasterApplication)
            modules(appModule)
        }
    }
}
