package br.com.tiago.obramaster.core.auth

import android.content.Context
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.auth

private const val NOME_APP_SECUNDARIO = "colaborador-provisioning"

/** Cria a conta do colaborador numa instância *secundária* e descartável do Firebase App —
 * ver ColaboradorProvisioner.kt. `Firebase.initialize(context, options, name)` e
 * `Firebase.auth(app)` são a API real do SDK (verificada linha a linha na tag v2.1.0 usada
 * neste projeto), não um workaround. */
class AndroidColaboradorProvisioner(private val context: Context) : ColaboradorProvisioner {

    override suspend fun criarConta(email: String, senha: String): ResultadoAuth {
        val app = try {
            Firebase.initialize(context, Firebase.app.options, NOME_APP_SECUNDARIO)
        } catch (e: IllegalStateException) {
            // App secundário de uma tentativa anterior que não foi limpa (ex.: processo morto
            // no meio da operação) — reaproveita em vez de falhar.
            Firebase.app(NOME_APP_SECUNDARIO)
        }
        val authSecundario = Firebase.auth(app)
        return try {
            val resultado = authSecundario.createUserWithEmailAndPassword(email, senha)
            val user = resultado.user ?: return ResultadoAuth.Erro("Falha ao criar conta")
            ResultadoAuth.Sucesso(user.uid, user.email ?: email)
        } catch (e: FirebaseAuthUserCollisionException) {
            ResultadoAuth.EmailJaCadastrado
        } catch (e: Exception) {
            ResultadoAuth.Erro(e.message ?: "Erro ao criar conta")
        } finally {
            authSecundario.signOut()
            app.delete()
        }
    }
}
