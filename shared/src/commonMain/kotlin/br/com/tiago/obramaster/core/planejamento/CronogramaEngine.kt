package br.com.tiago.obramaster.core.planejamento

import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.StatusEtapa

/** SPEC_OBRA_MASTER.md §4.7 — indicador de atraso e janela de datas pro Gantt simplificado. */
object CronogramaEngine {

    /** Data prevista de fim já passou e a etapa não está concluída → atraso. */
    fun estaAtrasada(etapa: Etapa, hoje: Long): Boolean {
        val fimPrevisto = etapa.dataFim ?: return false
        return fimPrevisto < hoje && etapa.status != StatusEtapa.CONCLUIDA
    }

    /** Janela [início, fim] previstos que cobre todas as etapas com datas definidas — usada pra
     * escalar as barras do Gantt. Retorna null se nenhuma etapa tem datas previstas. */
    fun janelaPrevista(etapas: List<Etapa>): Pair<Long, Long>? {
        val inicios = etapas.mapNotNull { it.dataInicio }
        val fins = etapas.mapNotNull { it.dataFim }
        if (inicios.isEmpty() || fins.isEmpty()) return null
        return inicios.min() to fins.max()
    }
}
