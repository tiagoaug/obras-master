package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Guardado em memória (perde ao dar refresh na página) — mesma limitação já registrada nos
// outros dados da Web (repositórios em memória, ver InMemoryRepositories.kt); persistência de
// verdade (IndexedDB/OPFS) fica pra quando a Web tiver um motivo real de precisar sobreviver a
// um refresh (ex.: quando o backend da Fase 10 existir).
actual class ImageStore {
    private val imagens = mutableMapOf<String, ByteArray>()

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun save(image: ImageRef, compressQuality: Int): String {
        val chave = "planta_fundo_${Uuid.random()}"
        imagens[chave] = image.bytes
        return chave
    }

    actual suspend fun load(key: String): ByteArray? = imagens[key]

    actual suspend fun delete(key: String) {
        imagens.remove(key)
    }
}
