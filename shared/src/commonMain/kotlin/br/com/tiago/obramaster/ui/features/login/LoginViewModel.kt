package br.com.tiago.obramaster.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.auth.SessionManager
import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data class TelaLogin(val erro: String? = null, val autenticando: Boolean = false) : LoginUiState
    data class Autenticado(val colaborador: Colaborador) : LoginUiState
}

/**
 * Cuida só do login em si — a criação do Gestor no primeiro uso é responsabilidade do
 * onboarding (ver ui/features/onboarding), que decide se esta tela chega a aparecer.
 */
class LoginViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.TelaLogin())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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
