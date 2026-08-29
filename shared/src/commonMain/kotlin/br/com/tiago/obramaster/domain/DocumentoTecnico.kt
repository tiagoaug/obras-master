package br.com.tiago.obramaster.domain

/** SPEC_AREA_EXECUTOR.md §2.2 — arquivo que o próprio usuário anexou (norma comprada, manual de
 * fabricante, apostila, procedimento interno). [arquivoKey] referencia o DocumentStore (§4).
 * [textoExtraido] só é preenchido na Fase 8.7 (PdfTextExtractor) — usado exclusivamente pra
 * busca local, nunca enviado a servidor/IA sem consentimento explícito por pergunta. */
data class DocumentoTecnico(
    val id: String,
    val nome: String,
    val tipo: TipoDocumento,
    val categoria: CategoriaNorma,
    val arquivoKey: String,
    val tamanhoBytes: Long,
    val normaVinculadaId: String? = null,
    val tags: List<String> = emptyList(),
    val vinculadaEtapasTemplate: List<String> = emptyList(),
    val vinculadaMaterialId: String? = null,
    val textoExtraido: String? = null,
    val adicionadoEm: Long,
)

enum class TipoDocumento { NORMA_PROPRIA, MANUAL_FABRICANTE, APOSTILA, PROCEDIMENTO_INTERNO, OUTRO }

val TipoDocumento.labelPtBr: String
    get() = when (this) {
        TipoDocumento.NORMA_PROPRIA -> "Norma própria"
        TipoDocumento.MANUAL_FABRICANTE -> "Manual de fabricante"
        TipoDocumento.APOSTILA -> "Apostila"
        TipoDocumento.PROCEDIMENTO_INTERNO -> "Procedimento interno"
        TipoDocumento.OUTRO -> "Outro"
    }
