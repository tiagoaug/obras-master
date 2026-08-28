package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.PontoXY

/**
 * SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §4.1 — Tentativa 1 (extração vetorial), refinamento
 * opcional sobre a Fase 3.67. `segmentos` são as linhas/retângulos encontrados no content stream
 * da página, em coordenadas já convertidas pra "px" pela mesma convenção do DxfImporter (1
 * ponto PDF = 1/72 polegada, escala fixa e sempre conhecida — diferente de DXF/SVG, PDF nunca
 * precisa de calibração manual quando a extração vetorial funciona).
 *
 * Nota: a spec original usa o tipo `Par<PontoXY, PontoXY>`, que não existe em Kotlin — usamos
 * `Pair`, o tipo real da stdlib com a mesma função.
 */
data class GeometriaExtraidaPdf(
    val segmentos: List<Pair<PontoXY, PontoXY>>,
    val escalaDetectada: Double?,
)

expect class PdfVectorExtractor {
    /** Retorna null se a página não tiver conteúdo vetorial reconhecível (ou no iOS, sempre — ver PdfVectorExtractor.ios.kt). */
    suspend fun extrairGeometria(pdfBytes: ByteArray, pagina: Int): GeometriaExtraidaPdf?
}
