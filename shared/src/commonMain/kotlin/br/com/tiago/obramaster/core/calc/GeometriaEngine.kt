package br.com.tiago.obramaster.core.calc

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.tan

/** SPEC_OBRA_MASTER.md §4.12.3-4 — área e perímetro das figuras planas usadas em obra, mais área
 * irregular por coordenadas (fórmula de Shoelace). Todas as funções retornam null para entrada
 * inválida (medidas não positivas, polígono degenerado) em vez de lançar exceção. */
object GeometriaEngine {

    data class Ponto(val x: Double, val y: Double)

    // Quadrado
    fun areaQuadrado(lado: Double): Double? = if (lado > 0) lado * lado else null
    fun perimetroQuadrado(lado: Double): Double? = if (lado > 0) 4 * lado else null

    // Retângulo
    fun areaRetangulo(largura: Double, altura: Double): Double? =
        if (largura > 0 && altura > 0) largura * altura else null

    fun perimetroRetangulo(largura: Double, altura: Double): Double? =
        if (largura > 0 && altura > 0) 2 * (largura + altura) else null

    // Triângulo
    fun areaTriangulo(base: Double, altura: Double): Double? =
        if (base > 0 && altura > 0) base * altura / 2.0 else null

    /** Perímetro do triângulo dados os 3 lados — valida a desigualdade triangular. */
    fun perimetroTriangulo(ladoA: Double, ladoB: Double, ladoC: Double): Double? {
        if (ladoA <= 0 || ladoB <= 0 || ladoC <= 0) return null
        if (ladoA + ladoB <= ladoC || ladoA + ladoC <= ladoB || ladoB + ladoC <= ladoA) return null
        return ladoA + ladoB + ladoC
    }

    // Trapézio
    fun areaTrapezio(baseMaior: Double, baseMenor: Double, altura: Double): Double? =
        if (baseMaior > 0 && baseMenor > 0 && altura > 0) (baseMaior + baseMenor) * altura / 2.0 else null

    fun perimetroTrapezio(baseMaior: Double, baseMenor: Double, ladoA: Double, ladoB: Double): Double? =
        if (baseMaior > 0 && baseMenor > 0 && ladoA > 0 && ladoB > 0) baseMaior + baseMenor + ladoA + ladoB else null

    // Círculo
    fun areaCirculo(raio: Double): Double? = if (raio > 0) PI * raio * raio else null
    fun perimetroCirculo(raio: Double): Double? = if (raio > 0) 2 * PI * raio else null

    // Polígono regular (n lados, medida do lado)
    fun areaPoligonoRegular(numeroLados: Int, lado: Double): Double? {
        if (numeroLados < 3 || lado <= 0) return null
        return (numeroLados * lado * lado) / (4 * tan(PI / numeroLados))
    }

    fun perimetroPoligonoRegular(numeroLados: Int, lado: Double): Double? =
        if (numeroLados < 3 || lado <= 0) null else numeroLados * lado

    /** Área irregular por coordenadas — fórmula de Shoelace. Requer ao menos 3 pontos; o
     * resultado é o valor absoluto (não depende do sentido horário/anti-horário da lista). */
    fun areaIrregularShoelace(pontos: List<Ponto>): Double? {
        if (pontos.size < 3) return null
        var soma = 0.0
        for (i in pontos.indices) {
            val atual = pontos[i]
            val proximo = pontos[(i + 1) % pontos.size]
            soma += atual.x * proximo.y - proximo.x * atual.y
        }
        val area = abs(soma) / 2.0
        return if (area > 0) area else null
    }

    /** Perímetro irregular por coordenadas — soma das distâncias entre pontos consecutivos (fechando o polígono). */
    fun perimetroIrregular(pontos: List<Ponto>): Double? {
        if (pontos.size < 3) return null
        var soma = 0.0
        for (i in pontos.indices) {
            val atual = pontos[i]
            val proximo = pontos[(i + 1) % pontos.size]
            soma += sqrt((proximo.x - atual.x) * (proximo.x - atual.x) + (proximo.y - atual.y) * (proximo.y - atual.y))
        }
        return if (soma > 0) soma else null
    }
}
