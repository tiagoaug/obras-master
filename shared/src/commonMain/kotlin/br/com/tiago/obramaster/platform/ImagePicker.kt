package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef

/** SPEC_OBRA_MASTER_KMP.md §4 — foto/galeria pra usar como fundo de referência na Planta Baixa. */
expect class ImagePicker {
    suspend fun isAvailable(): Boolean
    suspend fun takePhoto(): ImageRef?

    /** [multiple] ainda não é suportado — seleção única em todas as plataformas por ora (uso atual: 1 imagem de fundo por planta). */
    suspend fun pickFromGallery(multiple: Boolean = false): List<ImageRef>
}
