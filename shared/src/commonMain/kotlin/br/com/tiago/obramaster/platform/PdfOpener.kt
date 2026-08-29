package br.com.tiago.obramaster.platform

/** SPEC_AREA_EXECUTOR.md §4 — abre um PDF já salvo no [DocumentStore] no visualizador nativo da
 * plataforma (Intent no Android, "Open In" no iOS, aba do navegador na Web) — o app não
 * renderiza PDF, só entrega os bytes pro sistema abrir. */
expect class PdfOpener {
    suspend fun isAvailable(): Boolean
    suspend fun abrir(pdfBytes: ByteArray, nomeArquivo: String): Boolean
}
