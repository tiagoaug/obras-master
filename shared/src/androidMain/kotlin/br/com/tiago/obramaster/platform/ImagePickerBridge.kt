package br.com.tiago.obramaster.platform

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Ponte pra galeria/câmera (precisam de Activity) — mesmo padrão do ContactsPermissionBridge.
 * PickVisualMedia (photo picker do sistema) e TakePicturePreview não pedem permissão em runtime.
 */
object ImagePickerBridge {
    private var galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var continuacaoGaleria: CancellableContinuation<Uri?>? = null
    private var continuacaoCamera: CancellableContinuation<Bitmap?>? = null

    fun registrar(activity: ComponentActivity) {
        galleryLauncher = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            continuacaoGaleria?.resume(uri) { _, _, _ -> }
            continuacaoGaleria = null
        }
        cameraLauncher = activity.registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            continuacaoCamera?.resume(bitmap) { _, _, _ -> }
            continuacaoCamera = null
        }
    }

    suspend fun escolherDaGaleria(): Uri? {
        val launcher = galleryLauncher ?: return null
        return suspendCancellableCoroutine { continuacao ->
            continuacaoGaleria = continuacao
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    suspend fun tirarFoto(): Bitmap? {
        val launcher = cameraLauncher ?: return null
        return suspendCancellableCoroutine { continuacao ->
            continuacaoCamera = continuacao
            launcher.launch(null)
        }
    }
}
