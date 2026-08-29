package br.com.tiago.obramaster.core.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.auth

private const val NOME_APP_SECUNDARIO = "colaborador-provisioning"

/** Ver AndroidColaboradorProvisioner — mesma lógica, iOS não precisa de `Context`
 * (`Firebase.initialize` aceita `context: Any? = null` nessa plataforma). */
class IosColaboradorProvisioner : ColaboradorProvisioner {

    override suspend fun criarConta(email: String, senha: String): ResultadoAuth {
        val app = try {
            Firebase.initialize(null, Firebase.app.options, NOME_APP_SECUNDARIO)
        } catch (e: IllegalStateException) {
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
