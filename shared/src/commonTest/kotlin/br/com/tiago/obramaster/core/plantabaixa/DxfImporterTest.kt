package br.com.tiago.obramaster.core.plantabaixa

import br.com.tiago.obramaster.domain.PontoXY
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DxfImporterTest {

    private fun assertProximo(esperado: Double, atual: Double, tolerancia: Double = 0.01) {
        assertTrue(abs(esperado - atual) <= tolerancia, "esperado=$esperado atual=$atual")
    }

    private fun montarDxf(vararg pares: Pair<Int, String>): String =
        pares.joinToString("\n") { (codigo, valor) -> "$codigo\n$valor" }

    @Test
    fun importar_comInsunitsMetros_calibraEscalaAutomaticamenteEUsaTextoProximoComoNome() {
        val dxf = montarDxf(
            0 to "SECTION", 2 to "HEADER",
            9 to "\$INSUNITS", 70 to "6",
            0 to "ENDSEC",
            0 to "SECTION", 2 to "ENTITIES",
            0 to "LWPOLYLINE", 8 to "COMODOS", 70 to "1",
            10 to "0.0", 20 to "0.0",
            10 to "4.0", 20 to "0.0",
            10 to "4.0", 20 to "3.0",
            10 to "0.0", 20 to "3.0",
            0 to "LINE", 8 to "PAREDES",
            10 to "0.0", 20 to "0.0", 11 to "4.0", 21 to "0.0",
            0 to "TEXT",
            10 to "2.0", 20 to "1.5", 1 to "Sala",
            0 to "ENDSEC", 0 to "EOF",
        )

        val resultado = DxfImporter.importar(dxf)

        assertEquals(UnidadeDxf.METROS, resultado.unidadeDetectada)
        assertProximo(100.0, resultado.escalaAutomaticaPxPorMetro!!)
        assertEquals(1, resultado.paredes.size)
        assertEquals(1, resultado.comodos.size)
        assertEquals("Sala", resultado.comodos[0].nome)
        assertProximo(12.0, resultado.comodos[0].areaM2)
        assertProximo(14.0, resultado.comodos[0].perimetroM)
        assertEquals(listOf("COMODOS", "PAREDES"), resultado.camadasEncontradas)
    }

    @Test
    fun importar_semInsunits_caiEmCalibracaoManualSemTravar() {
        // POLYLINE estilo antigo (VERTEX + SEQEND), sem $INSUNITS no HEADER.
        val dxf = montarDxf(
            0 to "SECTION", 2 to "ENTITIES",
            0 to "POLYLINE", 8 to "COMODOS", 70 to "1",
            0 to "VERTEX", 10 to "0.0", 20 to "0.0",
            0 to "VERTEX", 10 to "5.0", 20 to "0.0",
            0 to "VERTEX", 10 to "5.0", 20 to "2.0",
            0 to "VERTEX", 10 to "0.0", 20 to "2.0",
            0 to "SEQEND",
            0 to "ENDSEC",
        )

        val resultado = DxfImporter.importar(dxf)

        assertEquals(UnidadeDxf.DESCONHECIDA, resultado.unidadeDetectada)
        assertNull(resultado.escalaAutomaticaPxPorMetro)
        assertEquals(1, resultado.comodos.size)
        assertEquals(0.0, resultado.comodos[0].areaM2)
        assertEquals(
            listOf(PontoXY(0.0, 0.0), PontoXY(5.0, 0.0), PontoXY(5.0, 2.0), PontoXY(0.0, 2.0)),
            resultado.comodos[0].pontos,
        )
    }

    @Test
    fun importar_ignoraCircleEContaComoElementoIgnorado() {
        val dxf = montarDxf(
            0 to "SECTION", 2 to "ENTITIES",
            0 to "CIRCLE", 8 to "PAREDES", 10 to "0.0", 20 to "0.0", 40 to "1.0",
            0 to "ENDSEC",
        )

        val resultado = DxfImporter.importar(dxf)

        assertEquals(0, resultado.paredes.size)
        assertEquals(0, resultado.comodos.size)
        assertEquals(1, resultado.elementosIgnorados)
    }

    @Test
    fun importar_comFiltroDeCamadas_excluiElementosDaCamadaNaoSelecionada() {
        val dxf = montarDxf(
            0 to "SECTION", 2 to "ENTITIES",
            0 to "LINE", 8 to "PAREDES", 10 to "0.0", 20 to "0.0", 11 to "1.0", 21 to "0.0",
            0 to "LINE", 8 to "COTAS", 10 to "0.0", 20 to "0.0", 11 to "2.0", 21 to "0.0",
            0 to "ENDSEC",
        )

        val semFiltro = DxfImporter.importar(dxf)
        assertEquals(listOf("COTAS", "PAREDES"), semFiltro.camadasEncontradas)
        assertEquals(2, semFiltro.paredes.size)

        val comFiltro = DxfImporter.importar(dxf, camadasSelecionadas = setOf("PAREDES"))
        assertEquals(1, comFiltro.paredes.size)
        assertEquals(1, comFiltro.elementosIgnorados)
    }
}
