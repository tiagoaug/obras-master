package br.com.tiago.obramaster.core.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BudgetEngineTest {

    @Test
    fun saldo_subtraiGastosDoOrcamento() {
        assertEquals(4_000_00L, BudgetEngine.saldo(10_000_00L, listOf(3_000_00L, 3_000_00L)))
    }

    @Test
    fun saldo_semGastosEIgualAoOrcamento() {
        assertEquals(10_000_00L, BudgetEngine.saldo(10_000_00L, emptyList()))
    }

    @Test
    fun custoPorM2_calculaCorretamente() {
        // R$ 110.000,00 / 100m² = R$ 1.100,00/m²
        assertEquals(1_100_00L, BudgetEngine.custoPorM2(110_000_00L, 100.0))
    }

    @Test
    fun custoPorM2_retornaNuloSemArea() {
        assertNull(BudgetEngine.custoPorM2(110_000_00L, null))
        assertNull(BudgetEngine.custoPorM2(110_000_00L, 0.0))
    }

    @Test
    fun percentualConsumido_calculaCorretamente() {
        assertEquals(50.0, BudgetEngine.percentualConsumido(10_000_00L, listOf(5_000_00L)))
    }

    @Test
    fun percentualConsumido_zeroQuandoOrcamentoZero() {
        assertEquals(0.0, BudgetEngine.percentualConsumido(0L, listOf(5_000_00L)))
    }

    @Test
    fun faixaOrcamento_classificaCorretamente() {
        assertEquals(BudgetEngine.FaixaOrcamento.TRANQUILO, BudgetEngine.faixaOrcamento(10_000_00L, listOf(5_000_00L)))
        assertEquals(BudgetEngine.FaixaOrcamento.ATENCAO, BudgetEngine.faixaOrcamento(10_000_00L, listOf(8_500_00L)))
        assertEquals(BudgetEngine.FaixaOrcamento.ESTOURADO, BudgetEngine.faixaOrcamento(10_000_00L, listOf(10_500_00L)))
    }
}
