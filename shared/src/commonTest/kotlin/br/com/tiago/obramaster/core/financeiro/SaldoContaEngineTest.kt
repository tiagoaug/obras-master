package br.com.tiago.obramaster.core.financeiro

import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.TipoConta
import br.com.tiago.obramaster.domain.TipoMovimentoConta
import kotlin.test.Test
import kotlin.test.assertEquals

class SaldoContaEngineTest {

    private fun conta(saldoInicial: Long = 100_000L) = Conta(
        id = "conta1",
        nome = "Caixa da obra",
        tipo = TipoConta.CAIXA,
        saldoInicial = saldoInicial,
        dataSaldoInicial = 0L,
    )

    private fun movimento(
        tipo: TipoMovimentoConta,
        valor: Long,
        data: Long = 0L,
        contaId: String = "conta1",
    ) = MovimentoConta(id = "m-$tipo-$valor-$data", contaId = contaId, tipo = tipo, valor = valor, data = data, descricao = "teste")

    @Test
    fun calcular_saldoInicialMaisMovimentos() {
        val c = conta(saldoInicial = 100_000L)
        val movimentos = listOf(
            movimento(TipoMovimentoConta.RECEBIMENTO, 50_000L),
            movimento(TipoMovimentoConta.PAGAMENTO, 20_000L),
        )
        assertEquals(130_000L, SaldoContaEngine.calcular(c, movimentos))
    }

    @Test
    fun calcular_ignoraMovimentosDeOutraConta() {
        val c = conta()
        val movimentos = listOf(
            movimento(TipoMovimentoConta.RECEBIMENTO, 999_999L, contaId = "outraConta"),
        )
        assertEquals(100_000L, SaldoContaEngine.calcular(c, movimentos))
    }

    @Test
    fun calcular_respeitaAteData() {
        val c = conta()
        val movimentos = listOf(
            movimento(TipoMovimentoConta.RECEBIMENTO, 10_000L, data = 100L),
            movimento(TipoMovimentoConta.RECEBIMENTO, 20_000L, data = 200L),
        )
        assertEquals(110_000L, SaldoContaEngine.calcular(c, movimentos, ateData = 150L))
        assertEquals(130_000L, SaldoContaEngine.calcular(c, movimentos, ateData = 200L))
    }

    @Test
    fun calcular_transferenciaSaidaDiminuiEEntradaAumenta() {
        val c = conta(saldoInicial = 50_000L)
        val movimentos = listOf(
            movimento(TipoMovimentoConta.TRANSFERENCIA_SAIDA, 10_000L),
            movimento(TipoMovimentoConta.TRANSFERENCIA_ENTRADA, 5_000L),
        )
        assertEquals(45_000L, SaldoContaEngine.calcular(c, movimentos))
    }

    @Test
    fun extratoComSaldoCorrente_calculaLinhaALinhaEmOrdemCronologica() {
        val c = conta(saldoInicial = 0L)
        val movimentos = listOf(
            movimento(TipoMovimentoConta.RECEBIMENTO, 10_000L, data = 200L),
            movimento(TipoMovimentoConta.PAGAMENTO, 3_000L, data = 100L),
        )
        val extrato = SaldoContaEngine.extratoComSaldoCorrente(c, movimentos)
        // ordenado por data: pagamento (100L) primeiro, depois recebimento (200L)
        assertEquals(-3_000L, extrato[0].second)
        assertEquals(7_000L, extrato[1].second)
    }
}
