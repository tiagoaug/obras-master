package br.com.tiago.obramaster.core.auth

import br.com.tiago.obramaster.data.repository.ColaboradorRepository
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.platform.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_SESSION_COLABORADOR_ID = "session_colaborador_id"

class SessionManager(
    private val colaboradorRepository: ColaboradorRepository,
) {
    private val _colaboradorLogado = MutableStateFlow<Colaborador?>(null)
    val colaboradorLogado: StateFlow<Colaborador?> = _colaboradorLogado.asStateFlow()

    sealed interface LoginResult {
        data class Sucesso(val colaborador: Colaborador) : LoginResult
        data object LoginOuSenhaInvalidos : LoginResult
    }

    /** Tenta restaurar a sessão salva (opção "manter conectado"). */
    suspend fun restaurar() {
        val id = SecureStorage.get(KEY_SESSION_COLABORADOR_ID) ?: return
        _colaboradorLogado.value = colaboradorRepository.buscarPorId(id)
    }

    suspend fun login(login: String, senha: String, manterConectado: Boolean): LoginResult {
        val colaborador = colaboradorRepository.buscarPorLogin(login)
            ?: return LoginResult.LoginOuSenhaInvalidos

        val senhaValida = PasswordHasher.verify(senha, colaborador.salt, colaborador.senhaHash)
        if (!senhaValida) return LoginResult.LoginOuSenhaInvalidos

        _colaboradorLogado.value = colaborador
        if (manterConectado) {
            SecureStorage.put(KEY_SESSION_COLABORADOR_ID, colaborador.id)
        }
        return LoginResult.Sucesso(colaborador)
    }

    fun logout() {
        _colaboradorLogado.value = null
        SecureStorage.remove(KEY_SESSION_COLABORADOR_ID)
    }
}
