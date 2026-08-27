package br.com.tiago.obramaster

import androidx.compose.ui.window.ComposeUIViewController
import br.com.tiago.obramaster.core.di.appModule
import br.com.tiago.obramaster.core.di.platformModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    if (GlobalContext.getOrNull() == null) {
        startKoin { modules(appModule, platformModule) }
    }
    App()
}
