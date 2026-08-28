package br.com.tiago.obramaster.domain

/** SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §6 — registro de qual arquivo originou uma PlantaBaixa. */
data class ArquivoImportado(
    val id: String,
    val plantaId: String,
    val formatoOrigem: FormatoImportacao,
    val nomeArquivoOriginal: String,
    val escalaDetectadaAutomaticamente: Boolean,
    val unidadeOrigem: String?,
    val camadasImportadas: List<String> = emptyList(),
    val importadoEm: Long,
)

enum class FormatoImportacao { DXF, PDF, SVG, FOTO }
