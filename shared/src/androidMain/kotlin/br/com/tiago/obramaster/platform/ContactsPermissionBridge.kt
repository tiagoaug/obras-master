package br.com.tiago.obramaster.platform

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/** Ponte entre o pedido de permissão (precisa de Activity) e o ContactsProvider (não tem Activity). */
object ContactsPermissionBridge {
    private var launcher: ActivityResultLauncher<String>? = null
    private var continuacaoPendente: CancellableContinuation<Boolean>? = null

    fun registrar(activity: ComponentActivity) {
        launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
            continuacaoPendente?.resume(concedida) { _, _, _ -> }
            continuacaoPendente = null
        }
    }

    suspend fun solicitar(): Boolean {
        val launcherAtual = launcher ?: return false
        return suspendCancellableCoroutine { continuacao ->
            continuacaoPendente = continuacao
            launcherAtual.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}
