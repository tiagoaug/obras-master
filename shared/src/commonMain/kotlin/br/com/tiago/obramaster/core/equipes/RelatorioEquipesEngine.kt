package br.com.tiago.obramaster.core.equipes

import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.TipoRegistroTrabalho

data class ResumoTrabalho(
    val diasTrabalhados: Int,
    val totalAPagar: Long, // centavos, soma dos registros ainda não pagos
    val totalPago: Long, // centavos, soma dos registros já pagos
)

/** SPEC_OBRA_MASTER.md §4.3 — "Relatório por funcionário e por equipe: dias trabalhados, total a pagar, total pago." */
object RelatorioEquipesEngine {

    fun porPessoa(registros: List<RegistroTrabalho>): Map<String, ResumoTrabalho> =
        registros.groupBy { it.pessoaId }.mapValues { (_, doGrupo) -> resumir(doGrupo) }

    fun porEquipe(registros: List<RegistroTrabalho>, membrosIds: Set<String>): ResumoTrabalho =
        resumir(registros.filter { it.pessoaId in membrosIds })

    private fun resumir(registros: List<RegistroTrabalho>) = ResumoTrabalho(
        diasTrabalhados = registros.count { it.tipo == TipoRegistroTrabalho.DIARIA },
        totalAPagar = registros.filter { !it.pago }.sumOf { it.valor },
        totalPago = registros.filter { it.pago }.sumOf { it.valor },
    )
}
