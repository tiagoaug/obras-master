package br.com.tiago.obramaster.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.auth.PermissionEngine
import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.core.modules.ModuleRegistry
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val colaborador: Colaborador,
    val modulosVisiveis: List<AppModule> = emptyList(),
)

class HomeViewModel(
    private val colaborador: Colaborador,
    private val moduleRegistry: ModuleRegistry,
    private val permissaoRepository: PermissaoRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(colaborador))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val perms = permissaoRepository.listarPorColaborador(colaborador.id)
            moduleRegistry.state.collect { estadoModulos ->
                val visiveis = estadoModulos.values
                    .filter { it.enabled }
                    .map { it.module }
                    .filter { modulo -> PermissionEngine.canView(colaborador, perms, modulo) }
                    .sortedBy { it.labelPtBr }
                _uiState.value = HomeUiState(colaborador, visiveis)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { sessionManager.logout() }
    }
}
