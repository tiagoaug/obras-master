package br.com.tiago.obramaster.core.metas

import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.EscopoMeta
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.StatusEtapa
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.domain.TipoMeta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetaEngineTest {

    private fun lancamento(tipo: TipoLancamento, valor: Long, projetoId: String? = null, centroDeCustoId: String = "cc1") =
        LancamentoFinanceiro(
            id = "l", tipo = tipo, categoriaId = "cat", centroDeCustoId = centroDeCustoId,
            natureza = NaturezaLancamento.CONTABIL, projetoId = projetoId,
            descricao = "x", valor = valor, data = 0L, formaPagamento = "pix",
        )

    private fun etapa(progresso: Int, projetoId: String = "p1") =
        Etapa(id = "e", projetoId = projetoId, nome = "Etapa", ordem = 0, orcamentoEtapa = 0L, progressoPercent = progresso, status = StatusEtapa.EM_ANDAMENTO)

    @Test
    fun resultadoFinanceiro_receitasMenosDespesas() {
        val lancamentos = listOf(
            lancamento(TipoLancamento.RECEITA, 100_00),
            lancamento(TipoLancamento.DESPESA, 30_00),
        )
        assertEquals(70_00, MetaEngine.resultadoFinanceiro(lancamentos))
    }

    @Test
    fun lancamentosNoEscopo_projetoFiltraPorProjetoId() {
        val meta = Meta(id = "m", escopo = EscopoMeta.PROJETO, referenciaId = "p1", titulo = "t", tipo = TipoMeta.FINANCEIRA, valorAlvo = 100_00)
        val lancamentos = listOf(
            lancamento(TipoLancamento.RECEITA, 10_00, projetoId = "p1"),
            lancamento(TipoLancamento.RECEITA, 20_00, projetoId = "p2"),
        )
        val resultado = MetaEngine.lancamentosNoEscopo(meta, lancamentos)
        assertEquals(1, resultado.size)
        assertEquals(10_00, resultado.first().valor)
    }

    @Test
    fun lancamentosNoEscopo_setorFiltraPorCentroDeCusto() {
        val meta = Meta(id = "m", escopo = EscopoMeta.SETOR, referenciaId = "cc1", titulo = "t", tipo = TipoMeta.FINANCEIRA, valorAlvo = 100_00)
        val lancamentos = listOf(
            lancamento(TipoLancamento.RECEITA, 10_00, centroDeCustoId = "cc1"),
            lancamento(TipoLancamento.RECEITA, 20_00, centroDeCustoId = "cc2"),
        )
        assertEquals(1, MetaEngine.lancamentosNoEscopo(meta, lancamentos).size)
    }

    @Test
    fun etapasNoEscopo_setorSempreVazio() {
        val meta = Meta(id = "m", escopo = EscopoMeta.SETOR, referenciaId = "cc1", titulo = "t", tipo = TipoMeta.PROGRESSO, valorAlvo = 100)
        assertTrue(MetaEngine.etapasNoEscopo(meta, listOf(etapa(50))).isEmpty())
    }

    @Test
    fun progressoMedio_calculaMedia() {
        assertEquals(60, MetaEngine.progressoMedio(listOf(etapa(40), etapa(80))))
    }

    @Test
    fun progressoMedio_listaVaziaRetornaZero() {
        assertEquals(0, MetaEngine.progressoMedio(emptyList()))
    }

    @Test
    fun percentualAtingido_calculaProporcao() {
        assertEquals(50.0, MetaEngine.percentualAtingido(50_00, 100_00))
        assertEquals(0.0, MetaEngine.percentualAtingido(50_00, 0))
    }

    @Test
    fun percentualAtingido_podeSerNegativoOuUltrapassar100() {
        assertEquals(-20.0, MetaEngine.percentualAtingido(-20_00, 100_00))
        assertEquals(150.0, MetaEngine.percentualAtingido(150_00, 100_00))
    }

    @Test
    fun diasRestantes_calculaDiferencaEmDias() {
        val umDiaMs = 24L * 60 * 60 * 1000
        assertEquals(5L, MetaEngine.diasRestantes(prazo = 5 * umDiaMs, agora = 0L))
        assertEquals(null, MetaEngine.diasRestantes(prazo = null, agora = 0L))
    }

    @Test
    fun estaAtrasada_prazoNoPassadoENaoConcluida() {
        val meta = Meta(id = "m", escopo = EscopoMeta.GERAL, titulo = "t", tipo = TipoMeta.FINANCEIRA, valorAlvo = 100, prazo = 1000L)
        assertTrue(MetaEngine.estaAtrasada(meta, agora = 2000L))
        assertFalse(MetaEngine.estaAtrasada(meta.copy(concluida = true), agora = 2000L))
        assertFalse(MetaEngine.estaAtrasada(meta, agora = 500L))
    }
}
