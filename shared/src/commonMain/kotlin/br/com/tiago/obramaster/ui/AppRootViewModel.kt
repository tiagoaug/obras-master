package br.com.tiago.obramaster.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
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

/** Decide, uma vez ao abrir o app: onboarding (sem Gestor), login (com Gestor) ou sessão já ativa. */
class AppRootViewModel(
    private val colaboradorRepository: ColaboradorRepository,
    private val sessionManager: SessionManager,
    private val categoriaFinanceiraRepository: CategoriaFinanceiraRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppRootUiState>(AppRootUiState.Carregando)
    val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { categoriaFinanceiraRepository.garantirCategoriasPadrao() }
        viewModelScope.launch {
            sessionManager.restaurar()
            val colaboradorSessao = sessionManager.colaboradorLogado.value
            _uiState.value = when {
                colaboradorSessao != null -> AppRootUiState.Autenticado(colaboradorSessao)
                colaboradorRepository.existeAlgumColaborador() -> AppRootUiState.PrecisaLogin
                else -> AppRootUiState.PrecisaOnboarding
            }
        }
    }

    fun autenticado(colaborador: Colaborador) {
        _uiState.value = AppRootUiState.Autenticado(colaborador)
    }
}
