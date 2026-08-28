package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef

/**
 * SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §4.2 — Tentativa 2 (fallback como imagem): renderiza a
 * primeira página do PDF como imagem e entra no mesmo fluxo de foto + calibração manual já
 * existente (ImagePicker/ImageStore, Fase 3.6). Não é extração vetorial (Tentativa 1, §4.1,
 * fica pra Fase 3.68) — por isso devolve um [ImageRef], igual a uma foto, não geometria.
 */
expect class PdfImageRenderer {
    suspend fun isAvailable(): Boolean
    suspend fun renderizarPrimeiraPagina(pdfBytes: ByteArray): ImageRef?
}
