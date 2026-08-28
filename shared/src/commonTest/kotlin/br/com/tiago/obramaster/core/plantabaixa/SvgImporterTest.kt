package br.com.tiago.obramaster.core.plantabaixa

import br.com.tiago.obramaster.domain.PontoXY
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SvgImporterTest {

    private fun assertProximo(esperado: Double, atual: Double, tolerancia: Double = 0.01) {
        assertTrue(abs(esperado - atual) <= tolerancia, "esperado=$esperado atual=$atual")
    }

    @Test
    fun importar_rectSemUnidadeReal_naoDetectaEscalaMasGeraComodo() {
        val svg = """<svg width="400" height="300"><rect x="10" y="20" width="400" height="300"/></svg>"""

        val resultado = SvgImporter.importar(svg)

        assertFalse(resultado.escalaDetectadaAutomaticamente)
        assertNull(resultado.escalaAutomaticaPxPorMetro)
        assertEquals(1, resultado.comodos.size)
        assertEquals(0.0, resultado.comodos[0].areaM2)
        assertEquals(
            listOf(PontoXY(10.0, 20.0), PontoXY(410.0, 20.0), PontoXY(410.0, 320.0), PontoXY(10.0, 320.0)),
            resultado.comodos[0].pontos,
        )
    }

    @Test
    fun importar_comViewBoxEUnidadeMm_detectaEscalaAutomaticamente() {
        val svg = """<svg viewBox="0 0 400 300" width="4000mm" height="3000mm"><rect x="0" y="0" width="400" height="300"/></svg>"""

        val resultado = SvgImporter.importar(svg)

        assertTrue(resultado.escalaDetectadaAutomaticamente)
        assertProximo(100.0, resultado.escalaAutomaticaPxPorMetro!!)
        assertEquals(1, resultado.comodos.size)
        assertProximo(12.0, resultado.comodos[0].areaM2)
    }

    @Test
    fun importar_polygon_geraComodoFechado() {
        val svg = """<svg><polygon points="0,0 400,0 400,300 0,300"/></svg>"""

        val resultado = SvgImporter.importar(svg)

        assertEquals(1, resultado.comodos.size)
        assertEquals(
            listOf(PontoXY(0.0, 0.0), PontoXY(400.0, 0.0), PontoXY(400.0, 300.0), PontoXY(0.0, 300.0)),
            resultado.comodos[0].pontos,
        )
    }

    @Test
    fun importar_line_geraParede() {
        val svg = """<svg><line x1="0" y1="0" x2="100" y2="0"/></svg>"""

        val resultado = SvgImporter.importar(svg)

        assertEquals(1, resultado.paredes.size)
        assertEquals(PontoXY(0.0, 0.0), resultado.paredes[0].pontoInicio)
        assertEquals(PontoXY(100.0, 0.0), resultado.paredes[0].pontoFim)
    }

    @Test
    fun importar_polylineAberta_geraParedesEmSequencia() {
        val svg = """<svg><polyline points="0,0 10,0 10,10"/></svg>"""

        val resultado = SvgImporter.importar(svg)

        assertEquals(0, resultado.comodos.size)
        assertEquals(2, resultado.paredes.size)
    }

    @Test
    fun importar_pathFechadoComZ_geraComodoSemPontoDuplicado() {
        val svg = """<svg><path d="M0,0 L100,0 L100,100 L0,100 Z"/></svg>"""

        val resultado = SvgImporter.importar(svg)

        assertEquals(1, resultado.comodos.size)
        assertEquals(4, resultado.comodos[0].pontos.size)
        assertEquals(
            listOf(PontoXY(0.0, 0.0), PontoXY(100.0, 0.0), PontoXY(100.0, 100.0), PontoXY(0.0, 100.0)),
            resultado.comodos[0].pontos,
        )
    }

    @Test
    fun importar_pathAberto_geraParedes() {
        val svg = """<svg><path d="M0,0 L50,0 L50,50"/></svg>"""

        val resultado = SvgImporter.importar(svg)

        assertEquals(0, resultado.comodos.size)
        assertEquals(2, resultado.paredes.size)
    }
}
