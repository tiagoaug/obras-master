package br.com.tiago.obramaster.domain

enum class TipoRetencao { INSS, ISS, IRRF, OUTRO }

/**
 * SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §7 — retenção fiscal sobre um LancamentoFinanceiro
 * (comum em nota de mão de obra/empreitada). A spec não dá um `id` pra Retencao (é um value
 * object dentro da lista do lançamento), mas a persistência precisa de uma chave de linha —
 * mesma solução já usada em RateioLancamento.
 */
data class RetencaoLancamento(
    val id: String,
    val lancamentoId: String,
    val tipo: TipoRetencao,
    val percentual: Double,
    val valorCalculado: Long, // centavos
)
