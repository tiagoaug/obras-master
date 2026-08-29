package br.com.tiago.obramaster.platform

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Guardado em memória (perde ao dar refresh na página) — mesma limitação já registrada no
// ImageStore.wasmJs.kt; persistência de verdade (OPFS/IndexedDB) fica pra quando a Web tiver
// um motivo real de precisar sobreviver a um refresh.
actual class DocumentStore {
    private val documentos = mutableMapOf<String, ByteArray>()

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun salvar(pdfBytes: ByteArray, nome: String): String {
        val chave = "${Uuid.random()}.pdf"
        documentos[chave] = pdfBytes
        return chave
    }

    actual suspend fun abrir(key: String): ByteArray? = documentos[key]

    actual suspend fun excluir(key: String) {
        documentos.remove(key)
    }
}
