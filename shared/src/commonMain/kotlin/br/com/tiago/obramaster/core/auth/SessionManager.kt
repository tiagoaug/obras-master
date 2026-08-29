package br.com.tiago.obramaster.core.auth

import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.StateFlow

/** Fase 10 (pivô Firebase) — identidade vem do Firebase Auth, disponível só em Android/iOS (o SDK
 * `dev.gitlive:firebase-*` não publica alvo wasmJs, ver shared/build.gradle.kts). Por isso é uma
 * interface com `actual`-equivalente por plataforma via Koin (`FirebaseSessionManager` em
 * mobileMain, stub indisponível em wasmJsMain) — mesmo padrão de FileExporter/DatabaseDriverFactory,
 * só que resolvido por binding de Koin em vez de expect/actual direto (os tipos usados no
 * construtor de cada implementação não existem nas outras plataformas). */
interface SessionManager {
    val colaboradorLogado: StateFlow<Colaborador?>

    sealed interface LoginResult {
        data class Sucesso(val colaborador: Colaborador) : LoginResult
        data object LoginOuSenhaInvalidos : LoginResult
        data object ContaSemEmpresaVinculada : LoginResult
        data class Erro(val mensagem: String) : LoginResult
    }

    /** Reflete a sessão já persistida (chamado uma vez, ao abrir o app). */
    suspend fun restaurar()

    suspend fun login(email: String, senha: String): LoginResult

    /** [entrarComGoogle] já aceita um convite pendente automaticamente quando não existe
     * Colaborador pra esse uid mas existe um ConviteColaborador com o e-mail da conta Google. */
    suspend fun entrarComGoogle(idToken: String): LoginResult

    /** Só usado pelo onboarding (ver OnboardingEngine) — cria a conta Firebase Auth do Gestor
     * (sempre auto-cadastro, ver nota em FirebaseAuthGateway) e já grava o Colaborador dele. */
    suspend fun cadastrarGestor(nome: String, email: String, senha: String, empresaId: String): LoginResult

    /** Usado por quem recebeu um convite (ver ConviteColaborador) e não tem/não quer usar Google —
     * cria a conta Firebase Auth com e-mail/senha e, se houver convite pendente pra esse e-mail,
     * já vira Colaborador de verdade. Sem convite correspondente, a conta criada é apagada de
     * novo (não faz sentido logar sem empresa) e retorna erro. */
    suspend fun criarContaEAceitarConvite(nome: String, email: String, senha: String): LoginResult

    suspend fun logout()
}
