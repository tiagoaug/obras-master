@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.PontoXY
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import kotlin.js.Promise

/** 1 ponto PDF = 1/72 polegada — escala física real do content stream, sempre conhecida. */
private const val PX_POR_METRO_PDF = 72.0 / 0.0254

/**
 * pdf.js (mesma instância já carregada via <script> em index.html, ver PdfImageRenderer.wasmJs.kt
 * da Fase 3.67) expõe `page.getOperatorList()`, mas não separa moveTo/lineTo/rect em entradas
 * individuais — tudo fica agrupado num único operador `constructPath` cujos argumentos são
 * [subOps, coords, minMax] (ver src/display/canvas.js do pdf.js). Este snippet decodifica isso,
 * fica só com linha/retângulo/closePath (curvas ficam fora de escopo, igual ao Android — spec
 * §2.1 só pede "operadores de linha/retângulo") e devolve os segmentos como uma string de
 * números separados por vírgula (mais simples de trazer de volta pro Kotlin/Wasm do que um
 * array/objeto tipado).
 */
private fun extrairSegmentosPdfComoTexto(bytes: Uint8Array, pagina: Int): Promise<JsString> = js(
    """
    (function() {
        return window.pdfjsLib.getDocument({ data: bytes }).promise.then(function(pdfDoc) {
            return pdfDoc.getPage(pagina + 1);
        }).then(function(page) {
            return page.getOperatorList().then(function(opList) {
                var OPS = window.pdfjsLib.OPS;
                var numeros = [];
                for (var i = 0; i < opList.fnArray.length; i++) {
                    if (opList.fnArray[i] !== OPS.constructPath) continue;
                    var subOps = opList.argsArray[i][0];
                    var coords = opList.argsArray[i][1];
                    var j = 0;
                    var cx = 0, cy = 0, sx = 0, sy = 0;
                    for (var k = 0; k < subOps.length; k++) {
                        var op = subOps[k];
                        if (op === OPS.moveTo) {
                            cx = coords[j++]; cy = coords[j++];
                            sx = cx; sy = cy;
                        } else if (op === OPS.lineTo) {
                            var nx = coords[j++], ny = coords[j++];
                            numeros.push(cx, cy, nx, ny);
                            cx = nx; cy = ny;
                        } else if (op === OPS.rectangle) {
                            var rx = coords[j++], ry = coords[j++], rw = coords[j++], rh = coords[j++];
                            numeros.push(rx, ry, rx + rw, ry);
                            numeros.push(rx + rw, ry, rx + rw, ry + rh);
                            numeros.push(rx + rw, ry + rh, rx, ry + rh);
                            numeros.push(rx, ry + rh, rx, ry);
                            cx = rx; cy = ry; sx = rx; sy = ry;
                        } else if (op === OPS.curveTo) {
                            j += 6; cx = coords[j - 2]; cy = coords[j - 1];
                        } else if (op === OPS.curveTo2 || op === OPS.curveTo3) {
                            j += 4; cx = coords[j - 2]; cy = coords[j - 1];
                        } else if (op === OPS.closePath) {
                            numeros.push(cx, cy, sx, sy);
                            cx = sx; cy = sy;
                        }
                    }
                }
                return numeros.join(',');
            });
        });
    })()
    """
)

actual class PdfVectorExtractor {

    actual suspend fun extrairGeometria(pdfBytes: ByteArray, pagina: Int): GeometriaExtraidaPdf? {
        val texto = extrairSegmentosPdfComoTexto(pdfBytes.paraUint8Array(), pagina).await<JsString>().toString()
        if (texto.isBlank()) return null

        val numeros = texto.split(",").mapNotNull { it.toDoubleOrNull() }
        if (numeros.size < 4) return null

        val segmentos = mutableListOf<Pair<PontoXY, PontoXY>>()
        var indice = 0
        while (indice + 4 <= numeros.size) {
            segmentos += PontoXY(numeros[indice], numeros[indice + 1]) to PontoXY(numeros[indice + 2], numeros[indice + 3])
            indice += 4
        }
        return if (segmentos.isEmpty()) null else GeometriaExtraidaPdf(segmentos, PX_POR_METRO_PDF)
    }

    private fun ByteArray.paraUint8Array(): Uint8Array {
        val array = Uint8Array(size)
        for (indice in indices) array[indice] = this[indice]
        return array
    }
}
