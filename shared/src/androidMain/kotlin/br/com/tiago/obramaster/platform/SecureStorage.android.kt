package br.com.tiago.obramaster.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val NOME_PREFS = "obramaster_secure_prefs"

actual object SecureStorage {
    private lateinit var prefs: SharedPreferences

    /** Precisa ser chamado uma vez (Application.onCreate) antes do primeiro uso.
     * Se o arquivo criptografado não bater mais com a chave do Android Keystore
     * (AEADBadTagException — acontece em restauração de backup, reset de keystore após
     * atualização do sistema, ou reinstalações de teste) o app travaria pra sempre no
     * onCreate; aqui apagamos o arquivo corrompido e recriamos do zero. Isso só derruba a
     * sessão salva (o usuário loga de novo), não é dado que valha a pena tentar recuperar. */
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = runCatching { criarPrefsCriptografadas(context) }.getOrElse {
            context.deleteSharedPreferences(NOME_PREFS)
            criarPrefsCriptografadas(context)
        }
    }

    private fun criarPrefsCriptografadas(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            NOME_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual fun put(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun get(key: String): String? = prefs.getString(key, null)

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
