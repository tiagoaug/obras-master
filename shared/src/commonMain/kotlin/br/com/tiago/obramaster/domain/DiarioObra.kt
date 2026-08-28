package br.com.tiago.obramaster.domain

/** SPEC_OBRA_MASTER.md §4.8 — diário de obra com fotos por data/etapa.
 * fotosUris guarda as CHAVES retornadas por ImageStore.save() (não URIs reais) — mesmo padrão já
 * usado em PlantaBaixa.imagemFundoKey; o nome do campo segue literal a spec para minimizar desvio. */
data class DiarioObra(
    val id: String,
    val projetoId: String,
    val etapaId: String? = null,
    val data: Long, // epoch millis UTC
    val texto: String,
    val clima: String? = null,
    val fotosUris: List<String> = emptyList(),
    val ativo: Boolean = true,
)
