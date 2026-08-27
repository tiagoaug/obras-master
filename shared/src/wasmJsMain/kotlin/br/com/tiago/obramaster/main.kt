package br.com.tiago.obramaster

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import br.com.tiago.obramaster.core.di.appModule
import br.com.tiago.obramaster.core.di.platformModule
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin { modules(appModule, platformModule) }
    ComposeViewport(document.body!!) {
        App()
    }
}
