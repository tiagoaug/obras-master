package br.com.tiago.obramaster.platform

/** SPEC_AREA_EXECUTOR.md §4 — extrai o texto de um PDF anexado só pra indexar a busca local
 * (§3); o app nunca reconstrói/exibe a norma a partir desse texto, só abre o PDF original.
 * Retorna string vazia quando a extração falha ou não está disponível na plataforma (iOS, sem
 * PDFKit por ora) — o documento continua listado e abrindo normalmente, só não entra na busca
 * por conteúdo. */
expect class PdfTextExtractor {
    suspend fun extrairTexto(pdfBytes: ByteArray): String
}
