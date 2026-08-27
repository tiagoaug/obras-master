package br.com.tiago.obramaster

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.ui.features.configuracoes.ConfiguracoesScreen
import br.com.tiago.obramaster.ui.features.home.HomeScreen
import br.com.tiago.obramaster.ui.features.login.LoginScreen
import br.com.tiago.obramaster.ui.theme.ObraMasterTheme
import org.koin.compose.koinInject

private sealed interface TelaRaiz {
    data object Login : TelaRaiz
    data class Home(val colaborador: Colaborador) : TelaRaiz
    data class Configuracoes(val colaborador: Colaborador) : TelaRaiz
}

@Composable
fun App() {
    val prefsStore: AccessibilityPrefsStore = koinInject()
    val prefs by prefsStore.prefs.collectAsState()

    ObraMasterTheme(prefs = prefs) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var tela by remember { mutableStateOf<TelaRaiz>(TelaRaiz.Login) }

            when (val atual = tela) {
                TelaRaiz.Login -> LoginScreen(onAutenticado = { tela = TelaRaiz.Home(it) })

                is TelaRaiz.Home -> HomeScreen(
                    colaborador = atual.colaborador,
                    onAbrirConfiguracoes = { tela = TelaRaiz.Configuracoes(atual.colaborador) },
                    onLogout = { tela = TelaRaiz.Login },
                )

                is TelaRaiz.Configuracoes -> ConfiguracoesScreen(
                    colaboradorLogado = atual.colaborador,
                    onVoltar = { tela = TelaRaiz.Home(atual.colaborador) },
                )
            }
        }
    }
}
