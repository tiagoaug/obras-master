package br.com.tiago.obramaster.core.planejamento

import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.StatusEtapa
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CronogramaEngineTest {

    private fun etapa(
        dataInicio: Long? = null,
        dataFim: Long? = null,
        status: StatusEtapa = StatusEtapa.NAO_INICIADA,
    ) = Etapa(
        id = "e1", projetoId = "p1", nome = "Fundação", ordem = 0, orcamentoEtapa = 0L,
        dataInicio = dataInicio, dataFim = dataFim, status = status,
    )

    @Test
    fun estaAtrasada_semDataFimPrevista_naoEstaAtrasada() {
        assertFalse(CronogramaEngine.estaAtrasada(etapa(dataFim = null), hoje = 1_000L))
    }

    @Test
    fun estaAtrasada_dataFimNoPassadoENaoConcluida_estaAtrasada() {
        assertTrue(CronogramaEngine.estaAtrasada(etapa(dataFim = 500L, status = StatusEtapa.EM_ANDAMENTO), hoje = 1_000L))
    }

    @Test
    fun estaAtrasada_dataFimNoPassadoMasConcluida_naoEstaAtrasada() {
        assertFalse(CronogramaEngine.estaAtrasada(etapa(dataFim = 500L, status = StatusEtapa.CONCLUIDA), hoje = 1_000L))
    }

    @Test
    fun estaAtrasada_dataFimNoFuturo_naoEstaAtrasada() {
        assertFalse(CronogramaEngine.estaAtrasada(etapa(dataFim = 2_000L, status = StatusEtapa.EM_ANDAMENTO), hoje = 1_000L))
    }

    @Test
    fun janelaPrevista_semEtapasComDatas_retornaNull() {
        assertNull(CronogramaEngine.janelaPrevista(listOf(etapa())))
    }

    @Test
    fun janelaPrevista_cobreOMenorInicioEOMaiorFim() {
        val etapas = listOf(
            etapa(dataInicio = 100L, dataFim = 300L),
            etapa(dataInicio = 200L, dataFim = 500L),
            etapa(), // sem datas, ignorada
        )
        assertEquals(100L to 500L, CronogramaEngine.janelaPrevista(etapas))
    }
}
