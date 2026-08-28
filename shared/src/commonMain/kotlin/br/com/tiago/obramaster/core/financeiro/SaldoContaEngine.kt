package br.com.tiago.obramaster.core.financeiro

import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.TipoMovimentoConta

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §4.1 — "Saldo de conta = saldoInicial + soma dos movimentos até a data". */
object SaldoContaEngine {

    fun calcular(conta: Conta, movimentos: List<MovimentoConta>, ateData: Long? = null): Long {
        val relevantes = movimentos.filter { it.contaId == conta.id && (ateData == null || it.data <= ateData) }
        return conta.saldoInicial + relevantes.sumOf { it.sinal() * it.valor }
    }

    /** Saldo corrente linha a linha, na mesma ordem cronológica em que os movimentos são exibidos no extrato. */
    fun extratoComSaldoCorrente(conta: Conta, movimentosCronologicos: List<MovimentoConta>): List<Pair<MovimentoConta, Long>> {
        var saldo = conta.saldoInicial
        return movimentosCronologicos.sortedBy { it.data }.map { movimento ->
            saldo += movimento.sinal() * movimento.valor
            movimento to saldo
        }
    }

    /** AJUSTE tratado sempre como positivo nesta fase — não há tela pra criar um ajuste negativo ainda. */
    private fun MovimentoConta.sinal(): Long = when (tipo) {
        TipoMovimentoConta.RECEBIMENTO, TipoMovimentoConta.TRANSFERENCIA_ENTRADA, TipoMovimentoConta.AJUSTE -> 1L
        TipoMovimentoConta.PAGAMENTO, TipoMovimentoConta.TRANSFERENCIA_SAIDA -> -1L
    }
}
