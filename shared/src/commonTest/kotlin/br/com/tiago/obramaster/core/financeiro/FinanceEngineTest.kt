package br.com.tiago.obramaster.core.financeiro

import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.RateioLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FinanceEngineTest {

    private fun dataEm(ano: Int, mes: Int, dia: Int): Long =
        LocalDate(ano, mes, dia).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    private fun lancamento(
        tipo: TipoLancamento,
        valor: Long,
        data: Long,
        categoriaId: String = "cat1",
        centroDeCustoId: String = "centro1",
        natureza: NaturezaLancamento = NaturezaLancamento.CONTABIL,
        projetoId: String? = null,
    ) = LancamentoFinanceiro(
        id = "id-${valor}-${data}",
        tipo = tipo,
        categoriaId = categoriaId,
        centroDeCustoId = centroDeCustoId,
        natureza = natureza,
        projetoId = projetoId,
        descricao = "teste",
        valor = valor,
        data = data,
        formaPagamento = "PIX",
    )

    @Test
    fun totaisEcLucro_somamCorretamente() {
        val lancamentos = listOf(
            lancamento(TipoLancamento.RECEITA, 10_000L, dataEm(2026, 3, 1)),
            lancamento(TipoLancamento.RECEITA, 5_000L, dataEm(2026, 3, 5)),
            lancamento(TipoLancamento.DESPESA, 3_000L, dataEm(2026, 3, 10)),
        )
        assertEquals(15_000L, FinanceEngine.totalReceitas(lancamentos))
        assertEquals(3_000L, FinanceEngine.totalDespesas(lancamentos))
        assertEquals(12_000L, FinanceEngine.lucro(lancamentos))
    }

    @Test
    fun aplicarFiltro_porPeriodoProjetoNaturezaCentro() {
        val lancamentos = listOf(
            lancamento(TipoLancamento.DESPESA, 1_000L, dataEm(2026, 1, 15), projetoId = "p1", natureza = NaturezaLancamento.CONTABIL, centroDeCustoId = "c1"),
            lancamento(TipoLancamento.DESPESA, 2_000L, dataEm(2026, 2, 15), projetoId = "p2", natureza = NaturezaLancamento.NAO_CONTABIL, centroDeCustoId = "c2"),
        )

        val porProjeto = FinanceEngine.aplicarFiltro(lancamentos, FiltroFinanceiro(projetoId = "p1"))
        assertEquals(1, porProjeto.size)
        assertEquals(1_000L, porProjeto[0].valor)

        val porNatureza = FinanceEngine.aplicarFiltro(lancamentos, FiltroFinanceiro(natureza = NaturezaLancamento.NAO_CONTABIL))
        assertEquals(1, porNatureza.size)
        assertEquals(2_000L, porNatureza[0].valor)

        val porPeriodo = FinanceEngine.aplicarFiltro(lancamentos, FiltroFinanceiro(periodoInicio = dataEm(2026, 2, 1), periodoFim = dataEm(2026, 2, 28)))
        assertEquals(1, porPeriodo.size)
        assertEquals(2_000L, porPeriodo[0].valor)

        val porCentro = FinanceEngine.aplicarFiltro(lancamentos, FiltroFinanceiro(centroDeCustoId = "c1"))
        assertEquals(1, porCentro.size)
    }

    @Test
    fun aplicarFiltro_ignoraLancamentosInativos() {
        val ativo = lancamento(TipoLancamento.DESPESA, 1_000L, dataEm(2026, 1, 1))
        val inativo = ativo.copy(id = "inativo", ativo = false)
        val resultado = FinanceEngine.aplicarFiltro(listOf(ativo, inativo), FiltroFinanceiro())
        assertEquals(1, resultado.size)
    }

    @Test
    fun agruparPorCategoria_somaSoDespesasPorPadrao() {
        val lancamentos = listOf(
            lancamento(TipoLancamento.DESPESA, 1_000L, dataEm(2026, 1, 1), categoriaId = "materiais"),
            lancamento(TipoLancamento.DESPESA, 500L, dataEm(2026, 1, 2), categoriaId = "materiais"),
            lancamento(TipoLancamento.DESPESA, 2_000L, dataEm(2026, 1, 3), categoriaId = "mao_de_obra"),
            lancamento(TipoLancamento.RECEITA, 9_000L, dataEm(2026, 1, 4), categoriaId = "venda"),
        )
        val agrupado = FinanceEngine.agruparPorCategoria(lancamentos)
        assertEquals(1_500L, agrupado["materiais"])
        assertEquals(2_000L, agrupado["mao_de_obra"])
        assertFalse(agrupado.containsKey("venda"))
    }

    @Test
    fun agruparPorMes_eEvolucaoLucro_agrupamCorretamentePorMesAno() {
        val lancamentos = listOf(
            lancamento(TipoLancamento.RECEITA, 10_000L, dataEm(2026, 1, 10)),
            lancamento(TipoLancamento.DESPESA, 4_000L, dataEm(2026, 1, 20)),
            lancamento(TipoLancamento.RECEITA, 3_000L, dataEm(2026, 2, 5)),
        )
        val porMes = FinanceEngine.agruparPorMes(lancamentos)
        assertEquals(10_000L to 4_000L, porMes[MesAno(2026, 1)])
        assertEquals(3_000L to 0L, porMes[MesAno(2026, 2)])

        val evolucao = FinanceEngine.evolucaoLucroPorMes(lancamentos)
        assertEquals(listOf(MesAno(2026, 1) to 6_000L, MesAno(2026, 2) to 3_000L), evolucao)
    }

    @Test
    fun resultadoPorCentroDeCusto_receitaMenosDespesaPorCentro() {
        val lancamentos = listOf(
            lancamento(TipoLancamento.RECEITA, 5_000L, dataEm(2026, 1, 1), centroDeCustoId = "obraA"),
            lancamento(TipoLancamento.DESPESA, 2_000L, dataEm(2026, 1, 2), centroDeCustoId = "obraA"),
            lancamento(TipoLancamento.DESPESA, 1_000L, dataEm(2026, 1, 3), centroDeCustoId = "obraB"),
        )
        val resultado = FinanceEngine.resultadoPorCentroDeCusto(lancamentos)
        assertEquals(3_000L, resultado["obraA"])
        assertEquals(-1_000L, resultado["obraB"])
    }

    @Test
    fun rateioSomaCemPorCento_validaComToleranciaEFalhaQuandoNaoFecha() {
        assertTrue(FinanceEngine.rateioSomaCemPorCento(listOf(rateio(60.0), rateio(40.0))))
        assertTrue(FinanceEngine.rateioSomaCemPorCento(listOf(rateio(33.34), rateio(33.33), rateio(33.33))))
        assertFalse(FinanceEngine.rateioSomaCemPorCento(listOf(rateio(50.0), rateio(40.0))))
        assertFalse(FinanceEngine.rateioSomaCemPorCento(emptyList()))
    }

    private fun rateio(percentual: Double) = RateioLancamento(id = "r", lancamentoId = "l", centroDeCustoId = "c", percentual = percentual)

    @Test
    fun periodoPreset_calculaInicioCorretoParaCadaOpcao() {
        val agora = dataEm(2026, 3, 15)
        val (inicioMes, fimMes) = FinanceEngine.periodoPreset(PeriodoPreset.MES, agora)
        assertEquals(dataEm(2026, 3, 1), inicioMes)
        assertEquals(agora, fimMes)

        val (inicioAno, _) = FinanceEngine.periodoPreset(PeriodoPreset.ANO, agora)
        assertEquals(dataEm(2026, 1, 1), inicioAno)

        val (inicioSemana, _) = FinanceEngine.periodoPreset(PeriodoPreset.SEMANA, agora)
        assertEquals(dataEm(2026, 3, 8), inicioSemana)

        val (inicioHoje, _) = FinanceEngine.periodoPreset(PeriodoPreset.HOJE, agora)
        assertEquals(dataEm(2026, 3, 15), inicioHoje)
    }
}
