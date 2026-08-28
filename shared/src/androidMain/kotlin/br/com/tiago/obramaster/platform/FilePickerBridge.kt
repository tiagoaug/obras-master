package br.com.tiago.obramaster.platform

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/** Ponte pro seletor de arquivo genérico (precisa de Activity) — mesmo padrão do ImagePickerBridge. */
object FilePickerBridge {
    private var launcher: ActivityResultLauncher<Array<String>>? = null
    private var continuacao: CancellableContinuation<Uri?>? = null

    fun registrar(activity: ComponentActivity) {
        launcher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            continuacao?.resume(uri) { _, _, _ -> }
            continuacao = null
        }
    }

    suspend fun escolherArquivo(tiposMime: Array<String>): Uri? {
        val launcherAtual = launcher ?: return null
        return suspendCancellableCoroutine { cont ->
            continuacao = cont
            launcherAtual.launch(tiposMime)
        }
    }
}
