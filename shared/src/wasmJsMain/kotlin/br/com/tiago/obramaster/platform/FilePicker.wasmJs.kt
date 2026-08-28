@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ArquivoSelecionado
import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader

actual class FilePicker {
    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun escolherArquivo(extensoesAceitas: List<String>): ArquivoSelecionado? {
        val arquivo = escolherViaInput(extensoesAceitas) ?: return null
        val bytes = lerComoBytes(arquivo) ?: return null
        return ArquivoSelecionado(nomeArquivo = arquivo.name, bytes = bytes)
    }

    private suspend fun escolherViaInput(extensoesAceitas: List<String>): File? = suspendCancellableCoroutine { continuacao ->
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        if (extensoesAceitas.isNotEmpty()) {
            input.accept = extensoesAceitas.joinToString(",") { ".$it" }
        }
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
