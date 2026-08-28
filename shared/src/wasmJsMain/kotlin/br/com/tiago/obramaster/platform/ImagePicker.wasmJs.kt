@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef
import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader

actual class ImagePicker {
    actual suspend fun isAvailable(): Boolean = true

    // Sem captura de câmera dedicada na Web — o próprio seletor de arquivo já oferece "usar
    // câmera" em navegadores mobile quando o dispositivo tem uma.
    actual suspend fun takePhoto(): ImageRef? = pickFromGallery(false).firstOrNull()

    actual suspend fun pickFromGallery(multiple: Boolean): List<ImageRef> {
        val arquivo = escolherArquivo() ?: return emptyList()
        val bytes = lerComoBytes(arquivo) ?: return emptyList()
        return listOf(ImageRef(bytes))
    }

    private suspend fun escolherArquivo(): File? = suspendCancellableCoroutine { continuacao ->
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = {
            val arquivo = input.files?.item(0)
            continuacao.resume(arquivo) { _, _, _ -> }
        }
        input.click()
    }

    private suspend fun lerComoBytes(arquivo: File): ByteArray? = suspendCancellableCoroutine { continuacao ->
        val leitor = FileReader()
        leitor.onload = {
            val arrayBuffer = leitor.result
            val int8 = Int8Array(arrayBuffer as org.khronos.webgl.ArrayBuffer)
            val bytes = ByteArray(int8.length) { indice -> int8[indice] }
            continuacao.resume(bytes) { _, _, _ -> }
        }
        leitor.readAsArrayBuffer(arquivo)
    }
}
