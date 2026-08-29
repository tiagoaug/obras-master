package br.com.tiago.obramaster.ui.features.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.core.auth.SessionManager
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

data class ConfiguracoesUiState(
    val colaboradores: List<Colaborador> = emptyList(),
    val permissoes: List<Permissao> = emptyList(),
    val modulos: Map<AppModule, ModuleAvailability> = emptyMap(),
    val erroCriarColaborador: String? = null,
)

class ConfiguracoesViewModel(
    private val colaboradorRepository: ColaboradorRepository,
    private val permissaoRepository: PermissaoRepository,
    private val sessionManager: SessionManager,
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
                Triple(colaboradores, permissoes, modulos)
            }.collect { (colaboradores, permissoes, modulos) ->
                _uiState.value = _uiState.value.copy(colaboradores = colaboradores, permissoes = permissoes, modulos = modulos)
            }
        }
    }

    /** O Gestor cria a conta do colaborador direto — sem convite/autocadastro por e-mail (ver
     * SessionManager.criarColaborador e ColaboradorProvisioner). */
    fun criarColaborador(nome: String, email: String, senha: String) {
        viewModelScope.launch {
            val resultado = sessionManager.criarColaborador(nome, email, senha)
            val erro = when (resultado) {
                is SessionManager.LoginResult.Erro -> resultado.mensagem
                SessionManager.LoginResult.LoginOuSenhaInvalidos -> "E-mail ou senha inválidos"
                SessionManager.LoginResult.ContaSemEmpresaVinculada -> null
                is SessionManager.LoginResult.Sucesso -> null
            }
            _uiState.value = _uiState.value.copy(erroCriarColaborador = erro)
        }
    }

    fun erroCriarColaboradorConsumido() {
        _uiState.value = _uiState.value.copy(erroCriarColaborador = null)
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
