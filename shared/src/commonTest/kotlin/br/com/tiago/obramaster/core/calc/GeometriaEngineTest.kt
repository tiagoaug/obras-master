package br.com.tiago.obramaster.core.calc

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GeometriaEngineTest {

    private fun assertProximo(esperado: Double, obtido: Double?, tolerancia: Double = 1e-6) {
        assertNotNull(obtido)
        assertEquals(true, abs(esperado - obtido) < tolerancia, "esperado $esperado, obtido $obtido")
    }

    @Test
    fun quadrado_areaEPerimetro() {
        assertProximo(25.0, GeometriaEngine.areaQuadrado(5.0))
        assertProximo(20.0, GeometriaEngine.perimetroQuadrado(5.0))
        assertNull(GeometriaEngine.areaQuadrado(0.0))
    }

    @Test
    fun retangulo_areaEPerimetro() {
        assertProximo(50.0, GeometriaEngine.areaRetangulo(10.0, 5.0))
        assertProximo(30.0, GeometriaEngine.perimetroRetangulo(10.0, 5.0))
    }

    @Test
    fun triangulo_area() {
        assertProximo(12.0, GeometriaEngine.areaTriangulo(6.0, 4.0))
    }

    @Test
    fun triangulo_perimetro_desigualdadeTriangularInvalidaRetornaNull() {
        assertProximo(12.0, GeometriaEngine.perimetroTriangulo(3.0, 4.0, 5.0))
        assertNull(GeometriaEngine.perimetroTriangulo(1.0, 1.0, 10.0))
    }

    @Test
    fun trapezio_areaEPerimetro() {
        assertProximo(24.0, GeometriaEngine.areaTrapezio(10.0, 6.0, 3.0))
        assertProximo(24.0, GeometriaEngine.perimetroTrapezio(10.0, 6.0, 4.0, 4.0))
    }

    @Test
    fun circulo_areaEPerimetro() {
        assertProximo(314.159265, GeometriaEngine.areaCirculo(10.0), tolerancia = 1e-4)
        assertProximo(62.83185, GeometriaEngine.perimetroCirculo(10.0), tolerancia = 1e-4)
    }

    @Test
    fun poligonoRegular_hexagonoLado10() {
        // área do hexágono regular = (3√3/2)·l² ≈ 259.8076
        assertProximo(259.8076, GeometriaEngine.areaPoligonoRegular(6, 10.0), tolerancia = 1e-3)
        assertProximo(60.0, GeometriaEngine.perimetroPoligonoRegular(6, 10.0))
        assertNull(GeometriaEngine.areaPoligonoRegular(2, 10.0))
    }

    @Test
    fun areaIrregularShoelace_quadrado10x10() {
        val pontos = listOf(
            GeometriaEngine.Ponto(0.0, 0.0),
            GeometriaEngine.Ponto(10.0, 0.0),
            GeometriaEngine.Ponto(10.0, 10.0),
            GeometriaEngine.Ponto(0.0, 10.0),
        )
        assertProximo(100.0, GeometriaEngine.areaIrregularShoelace(pontos))
        assertProximo(40.0, GeometriaEngine.perimetroIrregular(pontos))
    }

    @Test
    fun areaIrregularShoelace_menosDeTresPontosRetornaNull() {
        val pontos = listOf(GeometriaEngine.Ponto(0.0, 0.0), GeometriaEngine.Ponto(1.0, 1.0))
        assertNull(GeometriaEngine.areaIrregularShoelace(pontos))
    }

    @Test
    fun areaIrregularShoelace_ordemHorariaOuAntiHorariaDaMesmoResultado() {
        val antiHorario = listOf(
            GeometriaEngine.Ponto(0.0, 0.0),
            GeometriaEngine.Ponto(4.0, 0.0),
            GeometriaEngine.Ponto(4.0, 3.0),
            GeometriaEngine.Ponto(0.0, 3.0),
        )
        val horario = antiHorario.reversed()
        assertProximo(12.0, GeometriaEngine.areaIrregularShoelace(antiHorario))
        assertProximo(12.0, GeometriaEngine.areaIrregularShoelace(horario))
    }
}
