package br.com.tiago.obramaster.core.equipes

import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.TipoRegistroTrabalho
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RelatorioEquipesEngineTest {

    private fun registro(
        pessoaId: String,
        tipo: TipoRegistroTrabalho,
        valor: Long,
        pago: Boolean = false,
    ) = RegistroTrabalho(
        id = "r-$pessoaId-$valor-$pago",
        pessoaId = pessoaId,
        projetoId = "projeto1",
        data = 0L,
        tipo = tipo,
        valor = valor,
        pago = pago,
    )

    @Test
    fun porPessoa_agregaDiasTrabalhadosETotaisPorPessoa() {
        val registros = listOf(
            registro("p1", TipoRegistroTrabalho.DIARIA, 10_000L, pago = true),
            registro("p1", TipoRegistroTrabalho.DIARIA, 10_000L, pago = false),
            registro("p1", TipoRegistroTrabalho.HORA_EXTRA, 5_000L, pago = false),
            registro("p2", TipoRegistroTrabalho.EMPREITADA_PARCELA, 50_000L, pago = true),
        )

        val resumo = RelatorioEquipesEngine.porPessoa(registros)

        assertEquals(2, resumo["p1"]!!.diasTrabalhados) // só conta DIARIA
        assertEquals(15_000L, resumo["p1"]!!.totalAPagar) // diária não paga + hora extra
        assertEquals(10_000L, resumo["p1"]!!.totalPago)

        assertEquals(0, resumo["p2"]!!.diasTrabalhados)
        assertEquals(0L, resumo["p2"]!!.totalAPagar)
        assertEquals(50_000L, resumo["p2"]!!.totalPago)

        assertNull(resumo["p3"])
    }

    @Test
    fun porEquipe_somaSoOsMembrosDaEquipe() {
        val registros = listOf(
            registro("p1", TipoRegistroTrabalho.DIARIA, 10_000L, pago = false),
            registro("p2", TipoRegistroTrabalho.DIARIA, 20_000L, pago = true),
            registro("p3", TipoRegistroTrabalho.DIARIA, 999_999L, pago = false), // fora da equipe
        )

        val resumo = RelatorioEquipesEngine.porEquipe(registros, membrosIds = setOf("p1", "p2"))

        assertEquals(2, resumo.diasTrabalhados)
        assertEquals(10_000L, resumo.totalAPagar)
        assertEquals(20_000L, resumo.totalPago)
    }
}
