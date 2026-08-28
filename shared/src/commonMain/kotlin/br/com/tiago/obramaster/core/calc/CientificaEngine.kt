package br.com.tiago.obramaster.core.calc

import kotlin.math.pow
import kotlin.math.sqrt

/** SPEC_OBRA_MASTER.md §4.12.1 — operações da calculadora científica, como objeto puro testável.
 * Toda função retorna null pra domínio inválido ou resultado não-finito (NaN/Infinity), em vez de
 * propagar esses valores especiais pra UI. */
object CientificaEngine {

    fun potencia(base: Double, expoente: Double): Double? = base.pow(expoente).paraResultadoValido()

    fun raizQuadrada(x: Double): Double? {
        if (x < 0) return null
        return sqrt(x).paraResultadoValido()
    }

    /** Raiz de índice [indice] — aceita índice ímpar de número negativo (ex.: raiz cúbica de -8 = -2). */
    fun raizN(x: Double, indice: Double): Double? {
        if (indice == 0.0) return null
        if (x < 0) {
            val indiceInteiro = indice.toInt()
            val ehIndiceImparValido = indiceInteiro.toDouble() == indice && indiceInteiro % 2 != 0
            if (!ehIndiceImparValido) return null
            return (-(-x).pow(1.0 / indice)).paraResultadoValido()
        }
        return x.pow(1.0 / indice).paraResultadoValido()
    }

    fun log10(x: Double): Double? {
        if (x <= 0) return null
        return kotlin.math.log10(x).paraResultadoValido()
    }

    fun ln(x: Double): Double? {
        if (x <= 0) return null
        return kotlin.math.ln(x).paraResultadoValido()
    }

    fun exp(x: Double): Double? = kotlin.math.exp(x).paraResultadoValido()

    /** Definido só pra inteiros não-negativos; acima de 170 estoura Double (retorna null). */
    fun fatorial(n: Int): Double? {
        if (n < 0) return null
        var resultado = 1.0
        for (i in 2..n) resultado *= i
        return resultado.paraResultadoValido()
    }

    fun percentual(x: Double): Double = x / 100.0

    private fun Double.paraResultadoValido(): Double? = if (this.isNaN() || this.isInfinite()) null else this
}
