package br.com.tiago.obramaster.core.di

import br.com.tiago.obramaster.core.assistant.TelaContextoHolder
import br.com.tiago.obramaster.core.modules.ModuleRegistry
import br.com.tiago.obramaster.core.onboarding.OnboardingConcluidoStore
import br.com.tiago.obramaster.core.onboarding.OnboardingDraftStore
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.data.repository.ManualRepository
import br.com.tiago.obramaster.data.repository.NormaABNTRepository
import br.com.tiago.obramaster.data.repository.SeedManualRepository
import br.com.tiago.obramaster.data.repository.SeedNormaABNTRepository
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.ui.features.areaexecutor.AreaExecutorViewModel
import br.com.tiago.obramaster.ui.features.assistente.AjudaViewModel
import br.com.tiago.obramaster.ui.features.assistente.AssistenteViewModel
import br.com.tiago.obramaster.ui.features.areaexecutor.BibliotecaManuaisViewModel
import br.com.tiago.obramaster.ui.features.metas.MetasViewModel
import br.com.tiago.obramaster.ui.features.compras.FornecedoresViewModel
import br.com.tiago.obramaster.ui.features.compras.PedidosCompraViewModel
import br.com.tiago.obramaster.ui.features.configuracoes.ConfigBdiViewModel
import br.com.tiago.obramaster.ui.features.configuracoes.ConfiguracoesViewModel
import br.com.tiago.obramaster.ui.features.equipes.EquipesViewModel
import br.com.tiago.obramaster.ui.features.equipes.FuncionariosViewModel
import br.com.tiago.obramaster.ui.features.equipes.GerarPagamentoViewModel
import br.com.tiago.obramaster.ui.features.equipes.RegistroTrabalhoViewModel
import br.com.tiago.obramaster.ui.features.equipes.RelatorioEquipesViewModel
import br.com.tiago.obramaster.ui.features.financeiro.CategoriasFinanceirasViewModel
import br.com.tiago.obramaster.ui.features.financeiro.CentrosDeCustoViewModel
import br.com.tiago.obramaster.ui.features.financeiro.ContasViewModel
import br.com.tiago.obramaster.ui.features.financeiro.ExtratoContaViewModel
import br.com.tiago.obramaster.ui.features.financeiro.FinanceiroDashboardViewModel
import br.com.tiago.obramaster.ui.features.financeiro.LancamentosViewModel
import br.com.tiago.obramaster.ui.features.home.HomeViewModel
import br.com.tiago.obramaster.ui.features.login.LoginViewModel
import br.com.tiago.obramaster.ui.AppRootViewModel
import br.com.tiago.obramaster.ui.features.cadastros.CoresViewModel
import br.com.tiago.obramaster.ui.features.cadastros.MateriaisViewModel
import br.com.tiago.obramaster.ui.features.cadastros.UnidadesMedidaViewModel
import br.com.tiago.obramaster.ui.features.onboarding.OnboardingViewModel
import br.com.tiago.obramaster.ui.features.orcamentos.OrcamentosViewModel
import br.com.tiago.obramaster.ui.features.pessoas.PessoasViewModel
import br.com.tiago.obramaster.ui.features.projetos.CronogramaViewModel
import br.com.tiago.obramaster.ui.features.projetos.DiarioObraViewModel
import br.com.tiago.obramaster.ui.features.vendas.VendasViewModel
import br.com.tiago.obramaster.ui.features.plantabaixa.PlantaBaixaViewModel
import br.com.tiago.obramaster.ui.features.projetos.ProjetoDetalheViewModel
import br.com.tiago.obramaster.ui.features.projetos.ProjetosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Repositórios (ColaboradorRepository, PermissaoRepository, ModuleConfigRepository,
 * EmpresaRepository, ContaRepository) e o banco (ObraMasterDatabase) NÃO entram aqui — são
 * registrados no módulo de cada plataforma, porque Android/iOS usam SQLDelight local e Web
 * (por enquanto, sem backend) usa repositórios em memória. Ver platformModule em cada source set.
 * SessionManager também é por plataforma desde o pivô Firebase (Fase 10) — só Android/iOS têm o
 * SDK do Firebase; ver FirebaseSessionManager em mobileMain vs. o stub em wasmJsMain.
 */
val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { ModuleRegistry(get(), get()) }
    single { AccessibilityPrefsStore(get()) }
    single { OnboardingDraftStore(get()) }
    single { OnboardingConcluidoStore(get()) }
    single<NormaABNTRepository> { SeedNormaABNTRepository() }
    single<ManualRepository> { SeedManualRepository() }
    single { TelaContextoHolder() }

    factory { AppRootViewModel(get(), get(), get()) }
    factory { AssistenteViewModel(get(), get()) }
    factory { AjudaViewModel(get()) }
    factory { LoginViewModel(get()) }
    factory { (colaborador: Colaborador) -> HomeViewModel(colaborador, get(), get(), get()) }
    factory { ConfiguracoesViewModel(get(), get(), get(), get(), get()) }
    factory { OnboardingViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { PessoasViewModel(get(), get()) }
    factory { CoresViewModel(get()) }
    factory { MateriaisViewModel(get(), get()) }
    factory { UnidadesMedidaViewModel(get()) }
    factory { ProjetosViewModel(get(), get(), get(), get()) }
    factory { (projetoId: String) -> ProjetoDetalheViewModel(projetoId, get(), get(), get(), get(), get()) }
    factory { (plantaId: String) -> PlantaBaixaViewModel(plantaId, get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { (projetoId: String) -> CronogramaViewModel(projetoId, get(), get(), get()) }
    factory { (projetoId: String) -> DiarioObraViewModel(projetoId, get(), get(), get(), get()) }
    factory { CategoriasFinanceirasViewModel(get()) }
    factory { CentrosDeCustoViewModel(get()) }
    factory { LancamentosViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { FinanceiroDashboardViewModel(get(), get(), get(), get(), get(), get()) }
    factory { ContasViewModel(get(), get()) }
    factory { (contaId: String) -> ExtratoContaViewModel(contaId, get(), get()) }
    factory { FuncionariosViewModel(get(), get()) }
    factory { EquipesViewModel(get(), get()) }
    factory { RegistroTrabalhoViewModel(get(), get(), get(), get(), get()) }
    factory { GerarPagamentoViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { RelatorioEquipesViewModel(get(), get(), get()) }
    factory { FornecedoresViewModel(get(), get()) }
    factory { PedidosCompraViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { ConfigBdiViewModel(get()) }
    factory { OrcamentosViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { VendasViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { AreaExecutorViewModel(get()) }
    factory { BibliotecaManuaisViewModel(get(), get(), get(), get(), get()) }
    factory { MetasViewModel(get(), get(), get(), get(), get()) }
}
