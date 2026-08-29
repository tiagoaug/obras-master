package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

/** SPEC_OBRA_MASTER.md §4.9. [referenciaId] é o projetoId (escopo PROJETO) ou o
 * centroDeCustoId (escopo SETOR) — null para GERAL. [valorAlvo] é em centavos para
 * FINANCEIRA, ou um percentual 0-100 para PRAZO/PROGRESSO (que só valem pra GERAL/PROJETO —
 * Centro de Custo não tem noção de progresso). [concluida] é marcada manualmente pelo usuário,
 * nunca calculada automaticamente. */
@Serializable
data class Meta(
    val id: String,
    val escopo: EscopoMeta,
    val referenciaId: String? = null,
    val titulo: String,
    val tipo: TipoMeta,
    val valorAlvo: Long,
    val prazo: Long? = null,
    val concluida: Boolean = false,
    val ativo: Boolean = true,
)

@Serializable
enum class EscopoMeta { GERAL, PROJETO, SETOR }
@Serializable
enum class TipoMeta { FINANCEIRA, PRAZO, PROGRESSO }
