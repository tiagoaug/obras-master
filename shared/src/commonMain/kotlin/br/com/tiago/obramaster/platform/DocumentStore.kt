package br.com.tiago.obramaster.platform

/** SPEC_AREA_EXECUTOR.md §4 — guarda os PDFs que o usuário anexa na Biblioteca de Manuais;
 * DocumentoTecnico.arquivoKey referencia a chave retornada por [salvar]. Modelado como o
 * ImageStore já existente (mesmo padrão save/load/delete por chave). */
expect class DocumentStore {
    suspend fun salvar(pdfBytes: ByteArray, nome: String): String
    suspend fun abrir(key: String): ByteArray?
    suspend fun excluir(key: String)
}
