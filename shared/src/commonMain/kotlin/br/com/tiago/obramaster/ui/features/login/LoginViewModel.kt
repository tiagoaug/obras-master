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
 * onboarding (ver ui/features/onboarding), que decide se esta tela chega a aparecer. Sessão
 * (Firebase Auth) já é persistida automaticamente pelo SDK entre reinícios do app — não existe
 * mais opção "manter conectado", é sempre assim.
 *
 * "Criar conta" aqui é só pra quem recebeu um convite (ver ConviteColaborador) — não confundir
 * com o cadastro do Gestor, que passa pelo onboarding.
 */
class LoginViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.TelaLogin())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.TelaLogin(autenticando = true)
            aplicarResultado(sessionManager.login(email, senha))
        }
    }

    fun entrarComGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.TelaLogin(autenticando = true)
            aplicarResultado(sessionManager.entrarComGoogle(idToken))
        }
    }

    fun criarContaEAceitarConvite(nome: String, email: String, senha: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.TelaLogin(autenticando = true)
            aplicarResultado(sessionManager.criarContaEAceitarConvite(nome, email, senha))
        }
    }

    private fun aplicarResultado(resultado: SessionManager.LoginResult) {
        _uiState.value = when (resultado) {
            is SessionManager.LoginResult.Sucesso -> LoginUiState.Autenticado(resultado.colaborador)
            SessionManager.LoginResult.LoginOuSenhaInvalidos -> LoginUiState.TelaLogin(erro = "E-mail ou senha inválidos")
            SessionManager.LoginResult.ContaSemEmpresaVinculada -> LoginUiState.TelaLogin(erro = "Essa conta não está vinculada a nenhuma empresa")
            is SessionManager.LoginResult.Erro -> LoginUiState.TelaLogin(erro = resultado.mensagem)
        }
    }
}
