package br.com.tiago.obramaster.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.core.onboarding.OnboardingConcluidoStore
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppRootUiState {
    data object Carregando : AppRootUiState
    data object PrecisaOnboarding : AppRootUiState
    data object PrecisaLogin : AppRootUiState
    data class Autenticado(val colaborador: Colaborador) : AppRootUiState
}

/** Decide, uma vez ao abrir o app: onboarding (aparelho nunca concluiu), login (aparelho já
 * concluiu mas sem sessão ativa) ou sessão já ativa (Firebase Auth restaura sozinho). */
class AppRootViewModel(
    private val sessionManager: SessionManager,
    private val onboardingConcluidoStore: OnboardingConcluidoStore,
    private val categoriaFinanceiraRepository: CategoriaFinanceiraRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppRootUiState>(AppRootUiState.Carregando)
    val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.restaurar()
            val colaboradorSessao = sessionManager.colaboradorLogado.value
            // Só depois de confirmar sessão: garantirCategoriasPadrao() lê/escreve em
            // empresas/{empresaId}/categoriasFinanceiras (Firestore, ver EmpresaContexto), que só
            // existe depois que sessionManager.restaurar() já resolveu a empresa do uid logado —
            // chamar sem sessão derruba o app (EmpresaContexto.exigir() lança).
            if (colaboradorSessao != null) {
                categoriaFinanceiraRepository.garantirCategoriasPadrao()
            }
            _uiState.value = when {
                colaboradorSessao != null -> AppRootUiState.Autenticado(colaboradorSessao)
                onboardingConcluidoStore.concluido() -> AppRootUiState.PrecisaLogin
                else -> AppRootUiState.PrecisaOnboarding
            }
        }
    }

    fun autenticado(colaborador: Colaborador) {
        _uiState.value = AppRootUiState.Autenticado(colaborador)
        viewModelScope.launch { categoriaFinanceiraRepository.garantirCategoriasPadrao() }
    }
}
