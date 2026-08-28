package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef

/** SPEC_OBRA_MASTER_KMP.md §4 — guarda a imagem de fundo da Planta Baixa; PlantaBaixa.imagemFundoKey referencia a chave retornada. */
expect class ImageStore {
    suspend fun save(image: ImageRef, compressQuality: Int = 80): String
    suspend fun load(key: String): ByteArray?
    suspend fun delete(key: String)
}
