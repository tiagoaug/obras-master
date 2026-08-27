package br.com.tiago.obramaster.core.di

import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.core.modules.ModuleRegistry
import br.com.tiago.obramaster.core.prefs.AccessibilityPrefsStore
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.ui.features.configuracoes.ConfiguracoesViewModel
import br.com.tiago.obramaster.ui.features.home.HomeViewModel
import br.com.tiago.obramaster.ui.features.login.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Repositórios (ColaboradorRepository, PermissaoRepository, ModuleConfigRepository) e o
 * banco (ObraMasterDatabase) NÃO entram aqui — são registrados no módulo de cada plataforma,
 * porque Android/iOS usam SQLDelight local e Web (por enquanto, sem backend) usa um repositório
 * em memória. Ver platformModule em cada source set.
 */
val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { ModuleRegistry(get(), get()) }
    single { SessionManager(get()) }
    single { AccessibilityPrefsStore(get()) }

    factory { LoginViewModel(get(), get()) }
    factory { (colaborador: Colaborador) -> HomeViewModel(colaborador, get(), get(), get()) }
    factory { ConfiguracoesViewModel(get(), get(), get()) }
}
