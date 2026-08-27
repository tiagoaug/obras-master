package br.com.tiago.obramaster.android

import android.app.Application
import br.com.tiago.obramaster.core.di.appModule
import br.com.tiago.obramaster.core.di.platformModule
import br.com.tiago.obramaster.platform.SecureStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ObraMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        SecureStorage.init(this)

        startKoin {
            androidContext(this@ObraMasterApplication)
            modules(appModule, platformModule(this@ObraMasterApplication))
        }
    }
}
