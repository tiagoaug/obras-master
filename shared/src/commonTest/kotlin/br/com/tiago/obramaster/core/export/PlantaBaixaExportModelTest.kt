package br.com.tiago.obramaster.core.export

import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.domain.PontoXY
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlantaBaixaExportModelTest {

    private fun comodoQuadrado(lado: Double) = Comodo(
        id = "c1", plantaId = "p1", nome = "Sala",
        pontos = listOf(PontoXY(0.0, 0.0), PontoXY(lado, 0.0), PontoXY(lado, lado), PontoXY(0.0, lado)),
        corPreenchimento = "#FF0000", areaM2 = 0.0, perimetroM = 0.0,
    )

    @Test
    fun montar_semGeometriaRetornaVazio() {
        val resultado = PlantaBaixaExportModel.montar(emptyList(), emptyList(), 100.0, 800.0, 600.0, 20.0, false)
        assertTrue(resultado.comodos.isEmpty())
        assertTrue(resultado.paredes.isEmpty())
    }

    @Test
    fun montar_encaixaDentroDaAreaDisponivel() {
        val comodo = comodoQuadrado(400.0) // 400x400 px de editor
        val resultado = PlantaBaixaExportModel.montar(listOf(comodo), emptyList(), 100.0, 800.0, 600.0, margem = 20.0, inverterY = false)

        val xs = resultado.comodos.single().pontos.map { it.x }
        val ys = resultado.comodos.single().pontos.map { it.y }
        assertTrue(xs.min() >= 20.0 - 0.01, "ponto saiu da margem esquerda")
        assertTrue(xs.max() <= 780.0 + 0.01, "ponto saiu da margem direita")
        assertTrue(ys.min() >= 20.0 - 0.01, "ponto saiu da margem superior")
        assertTrue(ys.max() <= 580.0 + 0.01, "ponto saiu da margem inferior")
    }

    @Test
    fun montar_preservaProporcao() {
        // retângulo 2:1 (largura:altura) deve continuar 2:1 depois de escalado
        val comodo = Comodo(
            id = "c1", plantaId = "p1", nome = "Sala",
            pontos = listOf(PontoXY(0.0, 0.0), PontoXY(200.0, 0.0), PontoXY(200.0, 100.0), PontoXY(0.0, 100.0)),
            corPreenchimento = "#FF0000", areaM2 = 0.0, perimetroM = 0.0,
        )
        val resultado = PlantaBaixaExportModel.montar(listOf(comodo), emptyList(), 100.0, 800.0, 600.0, margem = 0.0, inverterY = false)
        val pontos = resultado.comodos.single().pontos
        val largura = pontos.maxOf { it.x } - pontos.minOf { it.x }
        val altura = pontos.maxOf { it.y } - pontos.minOf { it.y }
        assertTrue(abs(largura / altura - 2.0) < 0.01, "proporção não preservada: $largura x $altura")
    }

    @Test
    fun montar_inverterYReflexaCoordenadaVertical() {
        val comodo = comodoQuadrado(100.0)
        val semInverter = PlantaBaixaExportModel.montar(listOf(comodo), emptyList(), 100.0, 800.0, 600.0, margem = 0.0, inverterY = false)
        val comInverter = PlantaBaixaExportModel.montar(listOf(comodo), emptyList(), 100.0, 800.0, 600.0, margem = 0.0, inverterY = true)

        val yTopoSemInverter = semInverter.comodos.single().pontos.minOf { it.y }
        val yTopoComInverter = comInverter.comodos.single().pontos.maxOf { it.y }
        // o ponto que estava no topo (y mínimo) sem inversão deve virar o ponto de maior y com inversão
        assertTrue(abs((600.0 - yTopoSemInverter) - yTopoComInverter) < 0.01)
    }

    @Test
    fun montar_paredeGanhaEspessuraProporcionalAEscalaPxPorMetro() {
        val parede = Parede(id = "w1", plantaId = "p1", pontoInicio = PontoXY(0.0, 0.0), pontoFim = PontoXY(100.0, 0.0), espessuraCm = 20.0)
        // escalaPxPorMetro = 50 => 20cm = 0.2m => 10px de espessura na origem
        val resultado = PlantaBaixaExportModel.montar(emptyList(), listOf(parede), escalaPxPorMetro = 50.0, larguraSaida = 800.0, alturaSaida = 600.0, margem = 0.0, inverterY = false)
        assertTrue(resultado.paredes.single().espessuraSaida > 0.0)
    }

    @Test
    fun montar_rotuloEcorDoComodoSaoPreservados() {
        val comodo = comodoQuadrado(100.0)
        val resultado = PlantaBaixaExportModel.montar(listOf(comodo), emptyList(), 100.0, 800.0, 600.0, 20.0, false)
        assertEquals("Sala", resultado.comodos.single().rotulo)
        assertEquals("#FF0000", resultado.comodos.single().corHex)
    }
}
