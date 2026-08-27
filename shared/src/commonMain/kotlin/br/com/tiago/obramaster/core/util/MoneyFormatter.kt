package br.com.tiago.obramaster.core.util

import kotlin.math.abs

/** SPEC_OBRA_MASTER_KMP.md §3.2 — formatação manual (sem NumberFormat, que não existe em commonMain). */
object MoneyFormatter {

    fun formatar(centavos: Long): String {
        val negativo = centavos < 0
        val valorAbsoluto = abs(centavos)
        val reais = valorAbsoluto / 100
        val centavosParte = (valorAbsoluto % 100).toString().padStart(2, '0')
        val sinal = if (negativo) "-" else ""
        return "${sinal}R$ ${agruparMilhares(reais)},$centavosParte"
    }

    private fun agruparMilhares(valor: Long): String {
        val digitos = valor.toString()
        val builder = StringBuilder()
        for ((indice, char) in digitos.reversed().withIndex()) {
            if (indice != 0 && indice % 3 == 0) builder.append('.')
            builder.append(char)
        }
        return builder.reverse().toString()
    }
}
