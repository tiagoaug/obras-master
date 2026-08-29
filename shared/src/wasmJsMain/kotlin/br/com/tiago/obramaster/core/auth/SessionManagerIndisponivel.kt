package br.com.tiago.obramaster.core.auth

import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Fase 10 (pivô Firebase) — o SDK `dev.gitlive:firebase-*` não publica alvo wasmJs, então login
 * na Web ainda não está disponível (mesmo padrão de indisponibilidade já usado em outras telas
 * específicas de plataforma, ex.: FilePicker.ios.kt). */
class SessionManagerIndisponivel : SessionManager {
    override val colaboradorLogado: StateFlow<Colaborador?> = MutableStateFlow(null)

    override suspend fun restaurar() = Unit

    override suspend fun login(email: String, senha: String): SessionManager.LoginResult =
        SessionManager.LoginResult.Erro("Login ainda não está disponível na Web")

    override suspend fun entrarComGoogle(idToken: String): SessionManager.LoginResult =
        SessionManager.LoginResult.Erro("Login ainda não está disponível na Web")

    override suspend fun cadastrarGestor(nome: String, email: String, senha: String, empresaId: String): SessionManager.LoginResult =
        SessionManager.LoginResult.Erro("Onboarding ainda não está disponível na Web")

    override suspend fun criarColaborador(nome: String, email: String, senha: String): SessionManager.LoginResult =
        SessionManager.LoginResult.Erro("Ainda não está disponível na Web")

    override suspend fun logout() = Unit
}
