package br.com.tiago.obramaster.core.metas

import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.EscopoMeta
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.TipoLancamento

/** SPEC_OBRA_MASTER.md §4.9 — calcula `valorAtual` de uma Meta a partir de dados de outros
 * módulos (função pura, sem side-effects; quem busca os lançamentos/etapas é o ViewModel).
 *
 * FINANCEIRA: valorAtual = resultado acumulado (receitas − despesas) dos lançamentos ativos no
 * escopo da meta — sem corte de período, já que Meta não tem uma data de início, só o prazo.
 * PRAZO/PROGRESSO: valorAtual = média do progressoPercent das etapas no escopo — só valem pra
 * GERAL/PROJETO, Centro de Custo (escopo SETOR) não tem noção de progresso de obra. */
object MetaEngine {

    fun lancamentosNoEscopo(meta: Meta, lancamentos: List<LancamentoFinanceiro>): List<LancamentoFinanceiro> =
        when (meta.escopo) {
            EscopoMeta.GERAL -> lancamentos
            EscopoMeta.PROJETO -> lancamentos.filter { it.projetoId == meta.referenciaId }
            EscopoMeta.SETOR -> lancamentos.filter { it.centroDeCustoId == meta.referenciaId }
        }

    fun resultadoFinanceiro(lancamentos: List<LancamentoFinanceiro>): Long =
        lancamentos.sumOf { if (it.tipo == TipoLancamento.RECEITA) it.valor else -it.valor }

    fun etapasNoEscopo(meta: Meta, etapas: List<Etapa>): List<Etapa> =
        when (meta.escopo) {
            EscopoMeta.GERAL -> etapas
            EscopoMeta.PROJETO -> etapas.filter { it.projetoId == meta.referenciaId }
            EscopoMeta.SETOR -> emptyList()
        }

    fun progressoMedio(etapas: List<Etapa>): Int =
        if (etapas.isEmpty()) 0 else etapas.sumOf { it.progressoPercent } / etapas.size

    /** Pode passar de 100 (meta financeira superada) ou ser negativo (resultado no vermelho) —
     * quem exibe numa barra de progresso deve fazer coerceIn(0, 100) na hora de desenhar. */
    fun percentualAtingido(valorAtual: Long, valorAlvo: Long): Double =
        if (valorAlvo == 0L) 0.0 else valorAtual.toDouble() / valorAlvo.toDouble() * 100.0

    fun diasRestantes(prazo: Long?, agora: Long): Long? =
        prazo?.let { (it - agora) / (24L * 60 * 60 * 1000) }

    fun estaAtrasada(meta: Meta, agora: Long): Boolean {
        if (meta.concluida) return false
        return meta.prazo?.let { agora > it } ?: false
    }
}
