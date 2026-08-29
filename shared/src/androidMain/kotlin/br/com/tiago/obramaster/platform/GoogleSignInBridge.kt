package br.com.tiago.obramaster.platform

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Ponte pra "Entrar com Google" via Credential Manager (recomendado pelo Google desde 2024, no
 * lugar do GoogleSignInClient legado) — precisa de Activity pra mostrar o seletor de conta, mesmo
 * padrão de ImagePickerBridge/FilePickerBridge. O Web Client ID vem do google-services.json (é o
 * `oauth_client` com `client_type: 3`, criado automaticamente pelo Firebase quando o provedor
 * Google é habilitado no console — ver GoogleSignIn.android.kt). */
object GoogleSignInBridge {
    private var activity: ComponentActivity? = null

    fun registrar(activity: ComponentActivity) {
        this.activity = activity
    }

    suspend fun obterIdToken(webClientId: String): String? {
        val activity = activity ?: return null
        val credentialManager = CredentialManager.create(activity)
        val opcaoGoogle = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(opcaoGoogle).build()

        return try {
            val resultado = credentialManager.getCredential(activity, request)
            val credencial = resultado.credential
            if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(credencial.data).idToken
            } else {
                null
            }
        } catch (e: GetCredentialException) {
            Log.w("GoogleSignInBridge", "Falha ao obter credencial do Google", e)
            null
        } catch (e: GoogleIdTokenParsingException) {
            Log.w("GoogleSignInBridge", "Falha ao interpretar o idToken do Google", e)
            null
        }
    }
}
