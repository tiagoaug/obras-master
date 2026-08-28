@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class, kotlin.io.encoding.ExperimentalEncodingApi::class)

package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import kotlin.io.encoding.Base64
import kotlin.js.Promise

/**
 * pdf.js (carregado via <script> fixo em index.html, versão 3.11.174 — última a expor o global
 * clássico `pdfjsLib` sem exigir `<script type="module">`) faz todo o trabalho de decodificar o
 * PDF, renderizar a página 1 num <canvas> offscreen e devolver a imagem como data URL JPEG.
 * Um único snippet JS async cobre a sequência inteira (getDocument → getPage → render →
 * toDataURL) para minimizar o número de pontos de interop tipados manualmente.
 */
private fun renderizarPdfComoDataUrl(bytes: Uint8Array): Promise<JsString> = js(
    """
    (function() {
        return window.pdfjsLib.getDocument({ data: bytes }).promise.then(function(pdf) {
            return pdf.getPage(1);
        }).then(function(page) {
            var viewport = page.getViewport({ scale: 2 });
            var canvas = document.createElement('canvas');
            canvas.width = viewport.width;
            canvas.height = viewport.height;
            var ctx = canvas.getContext('2d');
            ctx.fillStyle = 'white';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            return page.render({ canvasContext: ctx, viewport: viewport }).promise.then(function() {
                return canvas.toDataURL('image/jpeg', 0.85);
            });
        });
    })()
    """
)

actual class PdfImageRenderer {
    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun renderizarPrimeiraPagina(pdfBytes: ByteArray): ImageRef? {
        val dataUrl = renderizarPdfComoDataUrl(pdfBytes.paraUint8Array()).await<JsString>().toString()
        val base64 = dataUrl.substringAfter(",", "")
        if (base64.isEmpty()) return null
        return ImageRef(Base64.decode(base64))
    }

    private fun ByteArray.paraUint8Array(): Uint8Array {
        val array = Uint8Array(size)
        for (indice in indices) array[indice] = this[indice]
        return array
    }
}
