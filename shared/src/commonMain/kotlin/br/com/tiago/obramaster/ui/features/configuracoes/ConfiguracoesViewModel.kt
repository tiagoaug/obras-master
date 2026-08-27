package br.com.tiago.obramaster.ui.features.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.core.modules.ModuleAvailability
import br.com.tiago.obramaster.core.modules.ModuleRegistry
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.data.repository.PermissaoRepository
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.domain.Permissao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ConfiguracoesUiState(
    val colaboradores: List<Colaborador> = emptyList(),
    val permissoes: List<Permissao> = emptyList(),
    val modulos: Map<AppModule, ModuleAvailability> = emptyMap(),
)

class ConfiguracoesViewModel(
    private val colaboradorRepository: ColaboradorRepository,
    private val permissaoRepository: PermissaoRepository,
    private val moduleRegistry: ModuleRegistry,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracoesUiState())
    val uiState: StateFlow<ConfiguracoesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                colaboradorRepository.observarAtivos(),
                permissaoRepository.observarTodas(),
                moduleRegistry.state,
            ) { colaboradores, permissoes, modulos ->
                ConfiguracoesUiState(colaboradores, permissoes, modulos)
            }.collect { _uiState.value = it }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun criarColaborador(nome: String, login: String, senha: String) {
        viewModelScope.launch {
            val hashed = PasswordHasher.hash(senha)
            colaboradorRepository.salvar(
                Colaborador(
                    id = Uuid.random().toString(),
                    nome = nome,
                    login = login,
                    senhaHash = hashed.hashBase64,
                    salt = hashed.saltBase64,
                    ativo = true,
                    ehGestor = false,
                ),
            )
        }
    }

    fun definirPermissao(colaboradorId: String, modulo: AppModule, nivel: NivelPermissao) {
        viewModelScope.launch { permissaoRepository.definir(colaboradorId, modulo.id, nivel) }
    }

    fun alternarModulo(modulo: AppModule, enabled: Boolean) {
        viewModelScope.launch { moduleRegistry.setEnabled(modulo, enabled) }
    }

    fun desativarColaborador(id: String) {
        viewModelScope.launch { colaboradorRepository.desativar(id) }
    }
}
