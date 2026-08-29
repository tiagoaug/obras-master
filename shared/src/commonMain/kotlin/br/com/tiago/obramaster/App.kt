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
import br.com.tiago.obramaster.ui.features.areaexecutor.AreaExecutorHomeScreen
import br.com.tiago.obramaster.ui.features.cadastros.CadastrosBasicosScreen
import br.com.tiago.obramaster.ui.features.calculadoras.CalculadorasModuloScreen
import br.com.tiago.obramaster.ui.features.compras.ComprasModuloScreen
import br.com.tiago.obramaster.ui.features.configuracoes.ConfiguracoesScreen
import br.com.tiago.obramaster.ui.features.equipes.EquipesModuloScreen
import br.com.tiago.obramaster.ui.features.financeiro.FinanceiroScreen
import br.com.tiago.obramaster.ui.features.home.HomeScreen
import br.com.tiago.obramaster.ui.features.login.LoginScreen
import br.com.tiago.obramaster.ui.features.metas.MetasScreen
import br.com.tiago.obramaster.ui.features.onboarding.OnboardingScreen
import br.com.tiago.obramaster.ui.features.orcamentos.OrcamentosScreen
import br.com.tiago.obramaster.ui.features.pessoas.PessoasScreen
import br.com.tiago.obramaster.ui.features.vendas.VendasScreen
import br.com.tiago.obramaster.ui.features.plantabaixa.EditorPlantaScreen
import br.com.tiago.obramaster.ui.features.projetos.CronogramaScreen
import br.com.tiago.obramaster.ui.features.projetos.DiarioObraScreen
import br.com.tiago.obramaster.ui.features.projetos.ProjetoDetalheScreen
import br.com.tiago.obramaster.ui.features.projetos.ProjetosScreen
import br.com.tiago.obramaster.ui.theme.ObraMasterTheme
import org.koin.compose.koinInject

private val MODULOS_IMPLEMENTADOS = setOf(AppModule.PESSOAS, AppModule.CADASTROS_BASE, AppModule.PROJETOS, AppModule.FINANCEIRO, AppModule.EQUIPES, AppModule.COMPRAS, AppModule.ORCAMENTOS, AppModule.VENDAS, AppModule.CALCULADORAS, AppModule.AREA_EXECUTOR, AppModule.METAS)

private sealed interface TelaRaiz {
    data object Onboarding : TelaRaiz
    data object Login : TelaRaiz
    data class Home(val colaborador: Colaborador) : TelaRaiz
    data class Configuracoes(val colaborador: Colaborador) : TelaRaiz
    data class Pessoas(val colaborador: Colaborador) : TelaRaiz
    data class CadastrosBasicos(val colaborador: Colaborador) : TelaRaiz
    data class Projetos(val colaborador: Colaborador) : TelaRaiz
    data class ProjetoDetalhe(val colaborador: Colaborador, val projetoId: String) : TelaRaiz
    data class EditorPlanta(val colaborador: Colaborador, val projetoId: String, val plantaId: String) : TelaRaiz
    data class Cronograma(val colaborador: Colaborador, val projetoId: String) : TelaRaiz
    data class DiarioObra(val colaborador: Colaborador, val projetoId: String) : TelaRaiz
    data class Financeiro(val colaborador: Colaborador) : TelaRaiz
    data class Equipes(val colaborador: Colaborador) : TelaRaiz
    data class Compras(val colaborador: Colaborador) : TelaRaiz
    data class Orcamentos(val colaborador: Colaborador) : TelaRaiz
    data class Vendas(val colaborador: Colaborador) : TelaRaiz
    data class Calculadoras(val colaborador: Colaborador) : TelaRaiz
    data class AreaExecutor(val colaborador: Colaborador) : TelaRaiz
    data class Metas(val colaborador: Colaborador) : TelaRaiz
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

                TelaRaiz.Onboarding -> OnboardingScreen(onConcluido = { appRootViewModel.autenticado(it) })

                TelaRaiz.Login -> LoginScreen(onAutenticado = { appRootViewModel.autenticado(it) })

                is TelaRaiz.Home -> HomeScreen(
                    colaborador = telaAtual.colaborador,
                    onAbrirConfiguracoes = { tela = TelaRaiz.Configuracoes(telaAtual.colaborador) },
                    onLogout = { tela = TelaRaiz.Login },
                    modulosImplementados = MODULOS_IMPLEMENTADOS,
                    onAbrirModulo = { modulo ->
                        tela = when (modulo) {
                            AppModule.PESSOAS -> TelaRaiz.Pessoas(telaAtual.colaborador)
                            AppModule.CADASTROS_BASE -> TelaRaiz.CadastrosBasicos(telaAtual.colaborador)
                            AppModule.PROJETOS -> TelaRaiz.Projetos(telaAtual.colaborador)
                            AppModule.FINANCEIRO -> TelaRaiz.Financeiro(telaAtual.colaborador)
                            AppModule.EQUIPES -> TelaRaiz.Equipes(telaAtual.colaborador)
                            AppModule.COMPRAS -> TelaRaiz.Compras(telaAtual.colaborador)
                            AppModule.ORCAMENTOS -> TelaRaiz.Orcamentos(telaAtual.colaborador)
                            AppModule.VENDAS -> TelaRaiz.Vendas(telaAtual.colaborador)
                            AppModule.CALCULADORAS -> TelaRaiz.Calculadoras(telaAtual.colaborador)
                            AppModule.AREA_EXECUTOR -> TelaRaiz.AreaExecutor(telaAtual.colaborador)
                            AppModule.METAS -> TelaRaiz.Metas(telaAtual.colaborador)
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

                is TelaRaiz.Projetos -> ProjetosScreen(
                    onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) },
                    onAbrirProjeto = { projetoId -> tela = TelaRaiz.ProjetoDetalhe(telaAtual.colaborador, projetoId) },
                )

                is TelaRaiz.ProjetoDetalhe -> ProjetoDetalheScreen(
                    projetoId = telaAtual.projetoId,
                    onVoltar = { tela = TelaRaiz.Projetos(telaAtual.colaborador) },
                    onAbrirPlanta = { plantaId ->
                        tela = TelaRaiz.EditorPlanta(telaAtual.colaborador, telaAtual.projetoId, plantaId)
                    },
                    onAbrirCronograma = { projetoId -> tela = TelaRaiz.Cronograma(telaAtual.colaborador, projetoId) },
                    onAbrirDiarioObra = { projetoId -> tela = TelaRaiz.DiarioObra(telaAtual.colaborador, projetoId) },
                )

                is TelaRaiz.EditorPlanta -> EditorPlantaScreen(
                    plantaId = telaAtual.plantaId,
                    onVoltar = { tela = TelaRaiz.ProjetoDetalhe(telaAtual.colaborador, telaAtual.projetoId) },
                )

                is TelaRaiz.Cronograma -> CronogramaScreen(
                    projetoId = telaAtual.projetoId,
                    onVoltar = { tela = TelaRaiz.ProjetoDetalhe(telaAtual.colaborador, telaAtual.projetoId) },
                )

                is TelaRaiz.DiarioObra -> DiarioObraScreen(
                    projetoId = telaAtual.projetoId,
                    onVoltar = { tela = TelaRaiz.ProjetoDetalhe(telaAtual.colaborador, telaAtual.projetoId) },
                )

                is TelaRaiz.Financeiro -> FinanceiroScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.Equipes -> EquipesModuloScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.Compras -> ComprasModuloScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.Orcamentos -> OrcamentosScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.Vendas -> VendasScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.Calculadoras -> CalculadorasModuloScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.AreaExecutor -> AreaExecutorHomeScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })

                is TelaRaiz.Metas -> MetasScreen(onVoltar = { tela = TelaRaiz.Home(telaAtual.colaborador) })
            }
        }
    }
}
