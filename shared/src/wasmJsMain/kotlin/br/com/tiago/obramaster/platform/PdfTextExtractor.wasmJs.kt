@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package br.com.tiago.obramaster.platform

import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import kotlin.js.Promise

/** pdf.js (mesmo script global já carregado em index.html pro PdfImageRenderer.wasmJs.kt) —
 * percorre todas as páginas via getTextContent() e concatena o texto. */
private fun extrairTextoDoPdf(bytes: Uint8Array): Promise<JsString> = js(
    """
    (function() {
        return window.pdfjsLib.getDocument({ data: bytes }).promise.then(function(pdf) {
            var paginas = [];
            for (var i = 1; i <= pdf.numPages; i++) paginas.push(i);
            return paginas.reduce(function(promessaAcumulada, numeroPagina) {
                return promessaAcumulada.then(function(textoAcumulado) {
                    return pdf.getPage(numeroPagina).then(function(page) {
                        return page.getTextContent();
                    }).then(function(conteudo) {
                        var textoPagina = conteudo.items.map(function(item) { return item.str; }).join(' ');
                        return textoAcumulado + textoPagina + '\\n';
                    });
                });
            }, Promise.resolve(''));
        });
    })()
    """
)

actual class PdfTextExtractor {
    actual suspend fun extrairTexto(pdfBytes: ByteArray): String =
        runCatching { extrairTextoDoPdf(pdfBytes.paraUint8Array()).await<JsString>().toString() }.getOrDefault("")

    private fun ByteArray.paraUint8Array(): Uint8Array {
        val array = Uint8Array(size)
        for (indice in indices) array[indice] = this[indice]
        return array
    }
}
