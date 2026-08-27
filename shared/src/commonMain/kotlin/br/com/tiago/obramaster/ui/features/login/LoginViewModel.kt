package br.com.tiago.obramaster.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface LoginUiState {
    data object Carregando : LoginUiState
    data object PrimeiroAcesso : LoginUiState
    data class TelaLogin(val erro: String? = null, val autenticando: Boolean = false) : LoginUiState
    data class Autenticado(val colaborador: Colaborador) : LoginUiState
}

class LoginViewModel(
    private val colaboradorRepository: ColaboradorRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Carregando)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.restaurar()
            val colaboradorSessao = sessionManager.colaboradorLogado.value
            if (colaboradorSessao != null) {
                _uiState.value = LoginUiState.Autenticado(colaboradorSessao)
                return@launch
            }
            val existeAlgumColaborador = colaboradorRepository.existeAlgumColaborador()
            _uiState.value = if (existeAlgumColaborador) LoginUiState.TelaLogin() else LoginUiState.PrimeiroAcesso
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun criarGestor(nome: String, login: String, senha: String) {
        viewModelScope.launch {
            val hashed = PasswordHasher.hash(senha)
            val gestor = Colaborador(
                id = Uuid.random().toString(),
                nome = nome,
                login = login,
                senhaHash = hashed.hashBase64,
                salt = hashed.saltBase64,
                ativo = true,
                ehGestor = true,
            )
            colaboradorRepository.salvar(gestor)
            sessionManager.login(login, senha, manterConectado = true)
            _uiState.value = LoginUiState.Autenticado(gestor)
        }
    }

    fun login(login: String, senha: String, manterConectado: Boolean) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.TelaLogin(autenticando = true)
            when (val resultado = sessionManager.login(login, senha, manterConectado)) {
                is SessionManager.LoginResult.Sucesso ->
                    _uiState.value = LoginUiState.Autenticado(resultado.colaborador)

                SessionManager.LoginResult.LoginOuSenhaInvalidos ->
                    _uiState.value = LoginUiState.TelaLogin(erro = "Login ou senha inválidos")
            }
        }
    }
}
