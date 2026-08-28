package br.com.tiago.obramaster.platform

import android.content.Context
import br.com.tiago.obramaster.domain.PontoXY
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.contentstream.PDFStreamEngine
import com.tom_roush.pdfbox.contentstream.operator.Operator
import com.tom_roush.pdfbox.cos.COSBase
import com.tom_roush.pdfbox.cos.COSNumber
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 1 ponto PDF = 1/72 polegada — escala física real do content stream, sempre conhecida. */
private const val PX_POR_METRO_PDF = 72.0 / 0.0254

private var pdfBoxInicializado = false

/**
 * PdfBox-Android (com.tom-roush:pdfbox-android) — percorre o content stream da página via
 * PDFStreamEngine, interceptando só os operadores de linha/retângulo (m/l/re/h), como a spec
 * pede (§2.1). Curvas (c/v/y) e estado gráfico complexo (clipping, Form XObjects aninhados)
 * ficam fora de escopo desta v1 — se a página não tiver nenhum desses operadores simples,
 * extrairGeometria devolve null e o fluxo cai pra Tentativa 2 (imagem), como já acontecia antes.
 */
actual class PdfVectorExtractor(private val context: Context) {

    actual suspend fun extrairGeometria(pdfBytes: ByteArray, pagina: Int): GeometriaExtraidaPdf? =
        withContext(Dispatchers.Default) {
            if (!pdfBoxInicializado) {
                PDFBoxResourceLoader.init(context.applicationContext)
                pdfBoxInicializado = true
            }
            runCatching {
                PDDocument.load(pdfBytes).use { documento ->
                    if (pagina < 0 || pagina >= documento.numberOfPages) return@use null
                    val extrator = ExtratorDeSegmentosPdf()
                    extrator.processPage(documento.getPage(pagina))
                    if (extrator.segmentos.isEmpty()) null else GeometriaExtraidaPdf(extrator.segmentos, PX_POR_METRO_PDF)
                }
            }.getOrNull()
        }
}

private class ExtratorDeSegmentosPdf : PDFStreamEngine() {
    val segmentos = mutableListOf<Pair<PontoXY, PontoXY>>()
    private var pontoAtual: PontoXY? = null
    private var inicioSubpath: PontoXY? = null

    override fun processOperator(operator: Operator, operands: MutableList<COSBase>) {
        when (operator.name) {
            "m" -> {
                val ponto = pontoTransformado(operands, 0)
                pontoAtual = ponto
                inicioSubpath = ponto
            }

            "l" -> {
                val ponto = pontoTransformado(operands, 0)
                pontoAtual?.let { segmentos += it to ponto }
                pontoAtual = ponto
            }

            "re" -> if (operands.size >= 4) {
                val x = numero(operands, 0)
                val y = numero(operands, 1)
                val largura = numero(operands, 2)
                val altura = numero(operands, 3)
                val p1 = transformedPoint(x, y).paraPontoXY()
                val p2 = transformedPoint(x + largura, y).paraPontoXY()
                val p3 = transformedPoint(x + largura, y + altura).paraPontoXY()
                val p4 = transformedPoint(x, y + altura).paraPontoXY()
                segmentos += p1 to p2
                segmentos += p2 to p3
                segmentos += p3 to p4
                segmentos += p4 to p1
                pontoAtual = p1
                inicioSubpath = p1
            }

            "h" -> {
                val inicio = inicioSubpath
                val atual = pontoAtual
                if (inicio != null && atual != null) segmentos += atual to inicio
            }
        }
        super.processOperator(operator, operands)
    }

    private fun numero(operands: List<COSBase>, indice: Int): Float =
        (operands.getOrNull(indice) as? COSNumber)?.floatValue() ?: 0f

    private fun pontoTransformado(operands: List<COSBase>, indiceBase: Int): PontoXY {
        val x = numero(operands, indiceBase)
        val y = numero(operands, indiceBase + 1)
        return transformedPoint(x, y).paraPontoXY()
    }

    private fun android.graphics.PointF.paraPontoXY() = PontoXY(x.toDouble(), y.toDouble())
}
