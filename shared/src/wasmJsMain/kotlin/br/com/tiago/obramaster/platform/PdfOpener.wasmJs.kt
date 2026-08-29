@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package br.com.tiago.obramaster.platform

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set

// Blob + URL.createObjectURL + window.open — o navegador abre o PDF no visualizador nativo dele
// numa nova aba, sem o app precisar renderizar nada.
private fun abrirPdfEmNovaAba(bytes: Uint8Array): Boolean = js(
    """
    (function() {
        try {
            var blob = new Blob([bytes], { type: 'application/pdf' });
            var url = URL.createObjectURL(blob);
            var aberto = window.open(url, '_blank');
            return aberto !== null;
        } catch (e) {
            return false;
        }
    })()
    """
)

actual class PdfOpener {
    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun abrir(pdfBytes: ByteArray, nomeArquivo: String): Boolean =
        abrirPdfEmNovaAba(pdfBytes.paraUint8Array())

    private fun ByteArray.paraUint8Array(): Uint8Array {
        val array = Uint8Array(size)
        for (indice in indices) array[indice] = this[indice]
        return array
    }
}
