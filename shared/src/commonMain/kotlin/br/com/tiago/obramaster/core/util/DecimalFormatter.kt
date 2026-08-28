package br.com.tiago.obramaster.core.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/** `String.format`/`NumberFormat` não existem em commonMain — arredondamento manual, sem exceções. */
object DecimalFormatter {
    fun formatar(valor: Double, casasDecimais: Int = 2): String {
        val fator = 10.0.pow(casasDecimais)
        val arredondado = round(valor * fator) / fator
        val negativo = arredondado < 0
        val absoluto = abs(arredondado)
        val parteInteira = absoluto.toLong()
        val parteDecimal = round((absoluto - parteInteira) * fator).toLong()
        val sinal = if (negativo) "-" else ""
        return if (casasDecimais <= 0) {
            "$sinal$parteInteira"
        } else {
            "$sinal$parteInteira,${parteDecimal.toString().padStart(casasDecimais, '0')}"
        }
    }
}
