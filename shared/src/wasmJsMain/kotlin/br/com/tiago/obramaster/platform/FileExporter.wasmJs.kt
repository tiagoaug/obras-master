@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package br.com.tiago.obramaster.platform

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set

// Blob + <a download> — padrão de "baixar arquivo" do navegador, sugerido pela própria
// SPEC_OBRA_MASTER_KMP.md §4.1 pra Web.
private fun baixarArquivo(bytes: Uint8Array, nome: String, mimeType: String): Boolean = js(
    """
    (function() {
        try {
            var blob = new Blob([bytes], { type: mimeType });
            var url = URL.createObjectURL(blob);
            var link = document.createElement('a');
            link.href = url;
            link.download = nome;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
            return true;
        } catch (e) {
            return false;
        }
    })()
    """
)

actual class FileExporter {
    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun compartilhar(nomeArquivo: String, bytes: ByteArray, mimeType: String): Boolean =
        baixarArquivo(bytes.paraUint8Array(), nomeArquivo, mimeType)

    private fun ByteArray.paraUint8Array(): Uint8Array {
        val array = Uint8Array(size)
        for (indice in indices) array[indice] = this[indice]
        return array
    }
}
