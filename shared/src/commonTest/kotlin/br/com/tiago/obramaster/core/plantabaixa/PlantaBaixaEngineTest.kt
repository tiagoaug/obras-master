package br.com.tiago.obramaster.core.plantabaixa

import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.PlantaBaixa
import br.com.tiago.obramaster.domain.PontoXY
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlantaBaixaEngineTest {

    private fun assertProximo(esperado: Double, atual: Double, tolerancia: Double = 0.01) {
        assertTrue(abs(esperado - atual) <= tolerancia, "esperado=$esperado atual=$atual")
    }

    @Test
    fun calcularAreaM2_retangulo10x100pxComEscala100pxPorMetro() {
        // Retângulo de 100x100 px, escala 100px = 1m => 1m x 1m = 1 m²
        val pontos = listOf(
            PontoXY(0.0, 0.0), PontoXY(100.0, 0.0), PontoXY(100.0, 100.0), PontoXY(0.0, 100.0),
        )
        assertProximo(1.0, PlantaBaixaEngine.calcularAreaM2(pontos, escalaPxPorMetro = 100.0))
    }

    @Test
    fun calcularAreaM2_retanguloMaior() {
        // 400x300 px, escala 100px/m => 4m x 3m = 12 m²
        val pontos = listOf(
            PontoXY(0.0, 0.0), PontoXY(400.0, 0.0), PontoXY(400.0, 300.0), PontoXY(0.0, 300.0),
        )
        assertProximo(12.0, PlantaBaixaEngine.calcularAreaM2(pontos, escalaPxPorMetro = 100.0))
    }

    @Test
    fun calcularAreaM2_poligonoEmL() {
        // L: quadrado 4x4 menos um quadrado 2x2 no canto = 16 - 4 = 12 (em unidades de escala 1px=1m)
        val pontos = listOf(
            PontoXY(0.0, 0.0), PontoXY(4.0, 0.0), PontoXY(4.0, 2.0),
            PontoXY(2.0, 2.0), PontoXY(2.0, 4.0), PontoXY(0.0, 4.0),
        )
        assertProximo(12.0, PlantaBaixaEngine.calcularAreaM2(pontos, escalaPxPorMetro = 1.0))
    }

    @Test
    fun calcularAreaM2_menosDeTresPontosRetornaZero() {
        assertEquals(0.0, PlantaBaixaEngine.calcularAreaM2(listOf(PontoXY(0.0, 0.0), PontoXY(1.0, 1.0)), 1.0))
    }

    @Test
    fun calcularPerimetroM_quadrado() {
        // Quadrado 100x100px, escala 100px/m => perímetro 4m
        val pontos = listOf(
            PontoXY(0.0, 0.0), PontoXY(100.0, 0.0), PontoXY(100.0, 100.0), PontoXY(0.0, 100.0),
        )
        assertProximo(4.0, PlantaBaixaEngine.calcularPerimetroM(pontos, escalaPxPorMetro = 100.0))
    }

    @Test
    fun areaTotalConstruida_somaSoDaPlantaCorreta() {
        val planta1 = PlantaBaixa(id = "p1", projetoId = "proj", nome = "Térreo", escalaPxPorMetro = 100.0, criadaEm = 0, atualizadaEm = 0)
        val comodos = listOf(
            Comodo(id = "c1", plantaId = "p1", nome = "Sala", pontos = emptyList(), corPreenchimento = "#fff", areaM2 = 10.0, perimetroM = 10.0),
            Comodo(id = "c2", plantaId = "p1", nome = "Quarto", pontos = emptyList(), corPreenchimento = "#fff", areaM2 = 8.0, perimetroM = 10.0),
            Comodo(id = "c3", plantaId = "outraPlanta", nome = "Fora", pontos = emptyList(), corPreenchimento = "#fff", areaM2 = 100.0, perimetroM = 10.0),
        )
        assertProximo(18.0, PlantaBaixaEngine.areaTotalConstruida(listOf(planta1), comodos))
    }

    @Test
    fun snapGrade_arredondaParaGradeMaisProxima() {
        val resultado = PlantaBaixaEngine.snapGrade(PontoXY(23.0, 47.0), tamanhoGradePx = 20.0)
        assertEquals(PontoXY(20.0, 40.0), resultado)
    }

    @Test
    fun snapAngulo_grudaEmNoventaGraus() {
        // ponto quase horizontal (89 graus) deve virar exatamente 90 graus (reto), mesma distância
        val anterior = PontoXY(0.0, 0.0)
        val atual = PontoXY(0.02, 10.0) // quase reto pra cima
        val resultado = PlantaBaixaEngine.snapAngulo(atual, anterior, grausSnap = 15.0)
        assertProximo(0.0, resultado.x)
        assertProximo(10.0, resultado.y)
    }

    @Test
    fun calcularEscala_distanciaConhecida() {
        // 200px correspondem a 2 metros reais => 100 px/m
        val escala = PlantaBaixaEngine.calcularEscala(PontoXY(0.0, 0.0), PontoXY(200.0, 0.0), distanciaRealM = 2.0)
        assertProximo(100.0, escala)
    }

    @Test
    fun poligonoFechado_verdadeiroQuandoUltimoPertoDoPrimeiro() {
        val pontos = listOf(PontoXY(0.0, 0.0), PontoXY(10.0, 0.0), PontoXY(10.0, 10.0), PontoXY(1.0, 1.0))
        assertTrue(PlantaBaixaEngine.poligonoFechado(pontos, toleranciaPx = 5.0))
    }

    @Test
    fun poligonoFechado_falsoQuandoAberto() {
        val pontos = listOf(PontoXY(0.0, 0.0), PontoXY(10.0, 0.0), PontoXY(10.0, 10.0), PontoXY(50.0, 50.0))
        assertFalse(PlantaBaixaEngine.poligonoFechado(pontos, toleranciaPx = 5.0))
    }
}
