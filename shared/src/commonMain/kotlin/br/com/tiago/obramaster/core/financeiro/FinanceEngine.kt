package br.com.tiago.obramaster.core.financeiro

import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.RateioLancamento
import br.com.tiago.obramaster.domain.RetencaoLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToLong

enum class PeriodoPreset { HOJE, SEMANA, MES, ANO }

data class FiltroFinanceiro(
    val periodoInicio: Long? = null, // epoch millis UTC, inclusive
    val periodoFim: Long? = null, // epoch millis UTC, inclusive
    val projetoId: String? = null,
    val natureza: NaturezaLancamento? = null, // null = Ambos (Contábil + Não Contábil)
    val centroDeCustoId: String? = null,
)

data class MesAno(val ano: Int, val mes: Int) : Comparable<MesAno> {
    override fun compareTo(other: MesAno): Int = compareValuesBy(this, other, { it.ano }, { it.mes })
    override fun toString(): String = "${mes.toString().padStart(2, '0')}/$ano"
}

/**
 * SPEC_OBRA_MASTER.md §4.2, SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §§2-5 — função pura, sem
 * side-effects: recebe listas de lançamentos + filtros, devolve totais e agrupamentos.
 */
object FinanceEngine {

    fun aplicarFiltro(lancamentos: List<LancamentoFinanceiro>, filtro: FiltroFinanceiro): List<LancamentoFinanceiro> =
        lancamentos.filter { lancamento ->
            lancamento.ativo &&
                (filtro.periodoInicio == null || lancamento.data >= filtro.periodoInicio) &&
                (filtro.periodoFim == null || lancamento.data <= filtro.periodoFim) &&
                (filtro.projetoId == null || lancamento.projetoId == filtro.projetoId) &&
                (filtro.natureza == null || lancamento.natureza == filtro.natureza) &&
                (filtro.centroDeCustoId == null || lancamento.centroDeCustoId == filtro.centroDeCustoId)
        }

    fun totalReceitas(lancamentos: List<LancamentoFinanceiro>): Long =
        lancamentos.filter { it.tipo == TipoLancamento.RECEITA }.sumOf { it.valor }

    fun totalDespesas(lancamentos: List<LancamentoFinanceiro>): Long =
        lancamentos.filter { it.tipo == TipoLancamento.DESPESA }.sumOf { it.valor }

    fun lucro(lancamentos: List<LancamentoFinanceiro>): Long = totalReceitas(lancamentos) - totalDespesas(lancamentos)

    /** Pizza por categoria (spec §4.2) — por padrão só despesas, o uso mais comum desse gráfico. */
    fun agruparPorCategoria(lancamentos: List<LancamentoFinanceiro>, tipo: TipoLancamento = TipoLancamento.DESPESA): Map<String, Long> =
        lancamentos.filter { it.tipo == tipo }.groupBy { it.categoriaId }.mapValues { (_, itens) -> itens.sumOf { it.valor } }

    fun mesAnoDe(epochMillis: Long): MesAno {
        val data = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.UTC).date
        return MesAno(data.year, data.monthNumber)
    }

    /** Barras por mês (spec §4.2) — receita e despesa lado a lado. */
    fun agruparPorMes(lancamentos: List<LancamentoFinanceiro>): Map<MesAno, Pair<Long, Long>> =
        lancamentos.groupBy { mesAnoDe(it.data) }.mapValues { (_, itens) -> totalReceitas(itens) to totalDespesas(itens) }

    /** Linha de evolução do lucro (spec §4.2) — lucro por mês, em ordem cronológica. */
    fun evolucaoLucroPorMes(lancamentos: List<LancamentoFinanceiro>): List<Pair<MesAno, Long>> =
        agruparPorMes(lancamentos).map { (mes, par) -> mes to (par.first - par.second) }.sortedBy { it.first }

    /** Resultado por Centro de Custo (SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §3). */
    fun resultadoPorCentroDeCusto(lancamentos: List<LancamentoFinanceiro>): Map<String, Long> =
        lancamentos.groupBy { it.centroDeCustoId }.mapValues { (_, itens) -> totalReceitas(itens) - totalDespesas(itens) }

    /** Critério de aceite: "Rateio de lançamento entre centros de custo soma sempre 100%". */
    fun rateioSomaCemPorCento(rateios: List<RateioLancamento>, toleranciaPercentual: Double = 0.01): Boolean =
        rateios.isNotEmpty() && abs(rateios.sumOf { it.percentual } - 100.0) <= toleranciaPercentual

    /** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §6/§7 — retenção fiscal sobre um valor bruto (ex.: INSS 11%). */
    fun calcularValorRetencao(valorBruto: Long, percentual: Double): Long = (valorBruto * percentual / 100.0).roundToLong()

    /** "cálculo automático do valor líquido a pagar" — valor bruto menos a soma de todas as retenções. */
    fun valorLiquido(valorBruto: Long, retencoes: List<RetencaoLancamento>): Long = valorBruto - retencoes.sumOf { it.valorCalculado }

    /** [agora] é parâmetro (não Clock.System direto) pra manter a função pura e testável. */
    fun periodoPreset(preset: PeriodoPreset, agora: Long): Pair<Long, Long> {
        val hoje = Instant.fromEpochMilliseconds(agora).toLocalDateTime(TimeZone.UTC).date
        val inicio = when (preset) {
            PeriodoPreset.HOJE -> hoje
            PeriodoPreset.SEMANA -> hoje.minus(DatePeriod(days = 7))
            PeriodoPreset.MES -> LocalDate(hoje.year, hoje.monthNumber, 1)
            PeriodoPreset.ANO -> LocalDate(hoje.year, 1, 1)
        }
        return inicio.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() to agora
    }
}
