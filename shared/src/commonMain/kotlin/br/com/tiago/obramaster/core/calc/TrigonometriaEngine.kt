package br.com.tiago.obramaster.core.calc

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** SPEC_OBRA_MASTER.md §4.12.2 — trigonometria completa: razões, inversas, Pitágoras, leis dos
 * senos/cossenos e resolução de triângulos (LLL, LAL, AAL). Ângulos sempre em graus na fronteira
 * pública — a conversão pra radianos é interna. */
object TrigonometriaEngine {

    private const val EPSILON = 1e-9

    fun grausParaRadianos(graus: Double): Double = graus * PI / 180.0
    fun radianosParaGraus(radianos: Double): Double = radianos * 180.0 / PI

    fun seno(anguloGraus: Double): Double = sin(grausParaRadianos(anguloGraus))
    fun cosseno(anguloGraus: Double): Double = cos(grausParaRadianos(anguloGraus))

    fun tangente(anguloGraus: Double): Double? {
        val cos = cosseno(anguloGraus)
        if (abs(cos) < EPSILON) return null
        return seno(anguloGraus) / cos
    }

    fun arcoSeno(valor: Double): Double? {
        if (valor < -1.0 || valor > 1.0) return null
        return radianosParaGraus(asin(valor))
    }

    fun arcoCosseno(valor: Double): Double? {
        if (valor < -1.0 || valor > 1.0) return null
        return radianosParaGraus(acos(valor))
    }

    fun arcoTangente(valor: Double): Double = radianosParaGraus(atan(valor))

    fun pitagorasHipotenusa(catetoA: Double, catetoB: Double): Double? {
        if (catetoA <= 0 || catetoB <= 0) return null
        return sqrt(catetoA * catetoA + catetoB * catetoB)
    }

    fun pitagorasCateto(hipotenusa: Double, catetoConhecido: Double): Double? {
        if (hipotenusa <= 0 || catetoConhecido <= 0 || catetoConhecido >= hipotenusa) return null
        return sqrt(hipotenusa * hipotenusa - catetoConhecido * catetoConhecido)
    }

    /** c² = a² + b² − 2ab·cos(C) — retorna o lado oposto ao ângulo informado. */
    fun leiDosCossenos(ladoA: Double, ladoB: Double, anguloCGraus: Double): Double? {
        if (ladoA <= 0 || ladoB <= 0) return null
        val valor = ladoA * ladoA + ladoB * ladoB - 2 * ladoA * ladoB * cosseno(anguloCGraus)
        if (valor < 0) return null
        return sqrt(valor)
    }

    /** a/sin(A) = b/sin(B) — dado o lado a e os ângulos A e B, retorna o lado b (oposto a B). */
    fun leiDosSenos(ladoA: Double, anguloAGraus: Double, anguloBGraus: Double): Double? {
        val senoA = seno(anguloAGraus)
        if (ladoA <= 0 || abs(senoA) < EPSILON) return null
        return ladoA * seno(anguloBGraus) / senoA
    }

    /** Convenção padrão: ângulo A é oposto ao lado a, ângulo B ao lado b, ângulo C ao lado c. */
    data class ResultadoTriangulo(
        val ladoA: Double, val ladoB: Double, val ladoC: Double,
        val anguloA: Double, val anguloB: Double, val anguloC: Double,
    )

    private fun anguloOposto(oposto: Double, adjacente1: Double, adjacente2: Double): Double? {
        val cos = (adjacente1 * adjacente1 + adjacente2 * adjacente2 - oposto * oposto) / (2 * adjacente1 * adjacente2)
        return arcoCosseno(cos.coerceIn(-1.0, 1.0))
    }

    /** Caso LLL: dados os 3 lados, valida a desigualdade triangular e calcula os 3 ângulos. */
    fun resolverTrianguloLLL(a: Double, b: Double, c: Double): ResultadoTriangulo? {
        if (a <= 0 || b <= 0 || c <= 0) return null
        if (a + b <= c || a + c <= b || b + c <= a) return null
        val anguloA = anguloOposto(a, b, c) ?: return null
        val anguloB = anguloOposto(b, a, c) ?: return null
        return ResultadoTriangulo(a, b, c, anguloA, anguloB, 180.0 - anguloA - anguloB)
    }

    /** Caso LAL: dados os lados a e b e o ângulo C entre eles, calcula o terceiro lado e os demais ângulos. */
    fun resolverTrianguloLAL(a: Double, b: Double, anguloCGraus: Double): ResultadoTriangulo? {
        if (a <= 0 || b <= 0 || anguloCGraus <= 0 || anguloCGraus >= 180.0) return null
        val c = leiDosCossenos(a, b, anguloCGraus) ?: return null
        if (c <= EPSILON) return null
        val anguloA = anguloOposto(a, b, c) ?: return null
        return ResultadoTriangulo(a, b, c, anguloA, 180.0 - anguloA - anguloCGraus, anguloCGraus)
    }

    /** Caso AAL/ALA: dado o lado a e os ângulos A e B, resolve os demais lados via lei dos senos. */
    fun resolverTrianguloAAL(ladoA: Double, anguloAGraus: Double, anguloBGraus: Double): ResultadoTriangulo? {
        if (ladoA <= 0 || anguloAGraus <= 0 || anguloBGraus <= 0) return null
        val anguloC = 180.0 - anguloAGraus - anguloBGraus
        if (anguloC <= 0) return null
        val ladoB = leiDosSenos(ladoA, anguloAGraus, anguloBGraus) ?: return null
        val ladoC = leiDosSenos(ladoA, anguloAGraus, anguloC) ?: return null
        return ResultadoTriangulo(ladoA, ladoB, ladoC, anguloAGraus, anguloBGraus, anguloC)
    }
}
