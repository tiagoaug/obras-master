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

    /** Login Google é reservado a Gestor/administrador de empresa — se não existe Colaborador
     * (nenhuma empresa administrada) pra esse uid, retorna [LoginResult.ContaSemEmpresaVinculada].
     * Colaboradores comuns não usam Google, são criados direto pelo Gestor (ver
     * [criarColaborador]). */
    suspend fun entrarComGoogle(idToken: String): LoginResult

    /** Só usado pelo onboarding (ver OnboardingEngine) — cria a conta Firebase Auth do Gestor
     * (sempre auto-cadastro, ver nota em FirebaseAuthGateway) e já grava o Colaborador dele. */
    suspend fun cadastrarGestor(nome: String, email: String, senha: String, empresaId: String): LoginResult

    /** Chamado pelo Gestor (Configurações → Colaboradores, ou pelo passo "Colaboradores" do
     * onboarding) — cria a conta Firebase Auth do colaborador direto, numa instância secundária
     * do Firebase App pra não derrubar a sessão de quem está criando (ver
     * [ColaboradorProvisioner]), e grava o Colaborador já vinculado à empresa atual
     * ([EmpresaContexto]). Não existe mais fluxo de convite/autocadastro por e-mail. */
    suspend fun criarColaborador(nome: String, email: String, senha: String): LoginResult

    suspend fun logout()
}
