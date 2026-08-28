package br.com.tiago.obramaster.platform

// SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §4.1, tabela da seção: "iOS | PDFKit não expõe
// geometria vetorial facilmente — nesta plataforma, pula direto pra Tentativa 2". Diferente dos
// outros deferimentos de iOS nesta sessão (que existem só porque não há Mac pra validar
// cinterop), este é o comportamento que a própria spec pede — não uma limitação deste ambiente.
actual class PdfVectorExtractor {
    actual suspend fun extrairGeometria(pdfBytes: ByteArray, pagina: Int): GeometriaExtraidaPdf? = null
}
