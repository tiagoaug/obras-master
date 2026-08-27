package br.com.tiago.obramaster.core.di

import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.core.modules.ModuleRegistry
import br.com.tiago.obramaster.core.onboarding.OnboardingDraftStore
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.ui.features.configuracoes.ConfiguracoesViewModel
import br.com.tiago.obramaster.ui.features.home.HomeViewModel
import br.com.tiago.obramaster.ui.features.login.LoginViewModel
import br.com.tiago.obramaster.ui.AppRootViewModel
import br.com.tiago.obramaster.ui.features.onboarding.OnboardingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Repositórios (ColaboradorRepository, PermissaoRepository, ModuleConfigRepository,
 * EmpresaRepository, ContaRepository) e o banco (ObraMasterDatabase) NÃO entram aqui — são
 * registrados no módulo de cada plataforma, porque Android/iOS usam SQLDelight local e Web
 * (por enquanto, sem backend) usa repositórios em memória. Ver platformModule em cada source set.
 */
val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { ModuleRegistry(get(), get()) }
    single { SessionManager(get()) }
    single { AccessibilityPrefsStore(get()) }
    single { OnboardingDraftStore(get()) }

    factory { AppRootViewModel(get(), get()) }
    factory { LoginViewModel(get()) }
    factory { (colaborador: Colaborador) -> HomeViewModel(colaborador, get(), get(), get()) }
    factory { ConfiguracoesViewModel(get(), get(), get()) }
    factory { OnboardingViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
