package br.com.tiago.obramaster.domain

enum class TipoCentroDeCusto { PROJETO, ADMINISTRATIVO, COMERCIAL, OUTRO }

/**
 * SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §3 — todo Projeto ganha um Centro de Custo 1:1 automático
 * (tipo PROJETO); os outros tipos cobrem despesas administrativas que não pertencem a nenhuma obra.
 */
data class CentroDeCusto(
    val id: String,
    val nome: String,
    val tipo: TipoCentroDeCusto,
    val projetoId: String? = null,
    val ativo: Boolean = true,
)

/** Divide um LancamentoFinanceiro percentualmente entre múltiplos centros de custo (§3 — deve somar 100%). */
data class RateioLancamento(
    val id: String,
    val lancamentoId: String,
    val centroDeCustoId: String,
    val percentual: Double,
)
