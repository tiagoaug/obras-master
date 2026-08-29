package br.com.tiago.obramaster.core.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Fase 10 (pivô Firebase) — fina camada sobre o Firebase Auth (via dev.gitlive:firebase-auth,
 * o wrapper multiplataforma; Android/iOS só, ver nota em shared/build.gradle.kts sobre wasmJs não
 * suportado). `Colaborador.id` passa a ser o `uid` retornado aqui. */
sealed interface ResultadoAuth {
    data class Sucesso(val uid: String, val email: String) : ResultadoAuth
    data object CredenciaisInvalidas : ResultadoAuth
    data object EmailJaCadastrado : ResultadoAuth
    data class Erro(val mensagem: String) : ResultadoAuth
}

object FirebaseAuthGateway {

    val uidAtual: String? get() = Firebase.auth.currentUser?.uid
    val emailAtual: String? get() = Firebase.auth.currentUser?.email

    val uidObservado: Flow<String?> get() = Firebase.auth.authStateChanged.map { it?.uid }

    suspend fun entrarComEmailSenha(email: String, senha: String): ResultadoAuth = try {
        val resultado = Firebase.auth.signInWithEmailAndPassword(email, senha)
        val user = resultado.user ?: return ResultadoAuth.CredenciaisInvalidas
        ResultadoAuth.Sucesso(user.uid, user.email ?: email)
    } catch (e: FirebaseAuthInvalidUserException) {
        ResultadoAuth.CredenciaisInvalidas
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        ResultadoAuth.CredenciaisInvalidas
    } catch (e: Exception) {
        ResultadoAuth.Erro(e.message ?: "Erro ao entrar")
    }

    /** Usado tanto pelo Gestor no onboarding quanto por um colaborador aceitando um convite
     * (ver ConviteColaborador em :core) — sempre auto-cadastro, nunca uma pessoa criando a
     * conta de outra (o Firebase Auth do lado do cliente não permite isso). */
    suspend fun cadastrarComEmailSenha(email: String, senha: String): ResultadoAuth = try {
        val resultado = Firebase.auth.createUserWithEmailAndPassword(email, senha)
        val user = resultado.user ?: return ResultadoAuth.Erro("Falha ao criar conta")
        ResultadoAuth.Sucesso(user.uid, user.email ?: email)
    } catch (e: FirebaseAuthUserCollisionException) {
        ResultadoAuth.EmailJaCadastrado
    } catch (e: Exception) {
        ResultadoAuth.Erro(e.message ?: "Erro ao criar conta")
    }

    suspend fun entrarComGoogle(idToken: String): ResultadoAuth = try {
        val credential = GoogleAuthProvider.credential(idToken, null)
        val resultado = Firebase.auth.signInWithCredential(credential)
        val user = resultado.user ?: return ResultadoAuth.Erro("Falha ao entrar com Google")
        ResultadoAuth.Sucesso(user.uid, user.email.orEmpty())
    } catch (e: Exception) {
        ResultadoAuth.Erro(e.message ?: "Erro ao entrar com Google")
    }

    suspend fun sair() {
        Firebase.auth.signOut()
    }

    /** Só usado pra desfazer um cadastro (ver SessionManager.criarContaEAceitarConvite) quando o
     * e-mail não corresponde a nenhum convite pendente — a conta recém-criada não serve pra nada
     * sem empresa vinculada. */
    suspend fun apagarContaAtual() {
        Firebase.auth.currentUser?.delete()
    }
}
