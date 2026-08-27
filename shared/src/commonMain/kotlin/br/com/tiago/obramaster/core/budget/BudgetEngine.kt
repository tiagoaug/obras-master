package br.com.tiago.obramaster.core.budget

/**
 * SPEC_OBRA_MASTER.md §4.1 — na spec original recebe List<Gasto>, mas "Gasto" é o lançamento
 * financeiro (Compras/Equipes/Financeiro), que só existe a partir da Fase 4/5/6. Opera direto
 * sobre os valores (Long centavos) para não depender de nenhuma entidade de fase futura — quando
 * essas fases existirem, é só mapear os lançamentos reais para uma lista de valores e chamar aqui.
 */
object BudgetEngine {

    fun saldo(orcamento: Long, gastos: List<Long>): Long = orcamento - gastos.sum()

    fun custoPorM2(gastoTotal: Long, areaM2: Double?): Long? {
        if (areaM2 == null || areaM2 <= 0.0) return null
        return (gastoTotal / areaM2).toLong()
    }

    /** 0.0..100.0+ (pode passar de 100 se estourou o orçamento). */
    fun percentualConsumido(orcamento: Long, gastos: List<Long>): Double {
        if (orcamento <= 0L) return 0.0
        return (gastos.sum().toDouble() / orcamento.toDouble()) * 100.0
    }

    enum class FaixaOrcamento { TRANQUILO, ATENCAO, ESTOURADO }

    /** Verde <80%, amarelo 80-100%, vermelho >100% — SPEC_OBRA_MASTER.md §4.1. */
    fun faixaOrcamento(orcamento: Long, gastos: List<Long>): FaixaOrcamento {
        val percentual = percentualConsumido(orcamento, gastos)
        return when {
            percentual > 100.0 -> FaixaOrcamento.ESTOURADO
            percentual >= 80.0 -> FaixaOrcamento.ATENCAO
            else -> FaixaOrcamento.TRANQUILO
        }
    }
}
