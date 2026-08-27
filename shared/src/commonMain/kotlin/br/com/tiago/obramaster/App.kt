package br.com.tiago.obramaster

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.ui.AppRootUiState
import br.com.tiago.obramaster.ui.AppRootViewModel
import br.com.tiago.obramaster.ui.features.cadastros.CadastrosBasicosScreen
import br.com.tiago.obramaster.ui.features.configuracoes.ConfiguracoesScreen
import br.com.tiago.obramaster.ui.features.home.HomeScreen
import br.com.tiago.obramaster.ui.features.login.LoginScreen
import br.com.tiago.obramaster.ui.features.onboarding.OnboardingScreen
import br.com.tiago.obramaster.ui.features.pessoas.PessoasScreen
import br.com.tiago.obramaster.ui.theme.ObraMasterTheme
import org.koin.compose.koinInject

private val MODULOS_IMPLEMENTADOS = setOf(AppModule.PESSOAS, AppModule.CADASTROS_BASE)

private sealed interface TelaRaiz {
    data object Onboarding : TelaRaiz
    data object Login : TelaRaiz
    data class Home(val colaborador: Colaborador) : TelaRaiz
    data class Configuracoes(val colaborador: Colaborador) : TelaRaiz
    data class Pessoas(val colaborador: Colaborador) : TelaRaiz
    data class CadastrosBasicos(val colaborador: Colaborador) : TelaRaiz
}

@Composable
fun App() {
    val prefsStore: AccessibilityPrefsStore = koinInject()
    val prefs by prefsStore.prefs.collectAsState()

    ObraMasterTheme(prefs = prefs) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val appRootViewModel: AppRootViewModel = koinInject()
            val rootState by appRootViewModel.uiState.collectAsState()
            var tela by remember { mutableStateOf<TelaRaiz?>(null) }

            val telaAtual = tela ?: when (val estado = rootState) {
                AppRootUiState.Carregando -> null
                AppRootUiState.PrecisaOnboarding -> TelaRaiz.Onboarding
                AppRootUiState.PrecisaLogin -> TelaRaiz.Login
                is AppRootUiState.Autenticado -> TelaRaiz.Home(estado.colaborador)
            }

            when (telaAtual) {
                null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                TelaRaiz.Onboarding -> OnboardingScreen(onConcluido = { tela = TelaRaiz.Home(it) })

                TelaRaiz.Login -> LoginScreen(onAutenticado = { tela = TelaRaiz.Home(it) })

                is TelaRaiz.Home -> HomeScreen(
                    colaborador = telaAtual.colaborador,
                    onAbrirConfiguracoes = { tela = TelaRaiz.Configuracoes(telaAtual.colaborador) },
                    onLogout = { tela = TelaRaiz.Login },
                    modulosImplementados = MODULOS_IMPLEMENTADOS,
                    onAbrirModulo = { modulo ->
                        tela = when (modulo) {
                            AppModule.PESSOAS -> TelaRaiz.Pessoas(telaAtual.colaborador)
                            AppModule.CADASTROS_BASE -> TelaRaiz.CadastrosBasicos(telaAtual.colaborador)
                            else -> telaAtual
                        }
                    },
                )

                is TelaRaiz.Configuracoes -> ConfiguracoesScreen(
                    colaboradorLogado = telaAtual.colaborador,
                    onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) },
                )

                is TelaRaiz.Pessoas -> PessoasScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.CadastrosBasicos -> CadastrosBasicosScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })
            }
        }
    }
}
