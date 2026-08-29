package br.com.tiago.obramaster.platform

/** SPEC_OBRA_MASTER_KMP.md §4, §4.1 — entrega os bytes já gerados (PDF/XLSX/JPG) pro sistema
 * salvar/compartilhar. Não gera o conteúdo — isso é do ExportEngine, em commonMain. */
expect class FileExporter {
    suspend fun isAvailable(): Boolean
    suspend fun compartilhar(nomeArquivo: String, bytes: ByteArray, mimeType: String): Boolean
}
