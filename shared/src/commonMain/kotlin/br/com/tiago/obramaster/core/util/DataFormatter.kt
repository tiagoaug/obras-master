package br.com.tiago.obramaster.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Sem DatePicker do Material3 aqui — essa versão do Compose Multiplatform já mostrou APIs M3
 * ausentes antes (ExposedDropdownMenu, Fase 1.5); campo de texto simples "dd/mm/aaaa" evita
 * repetir o mesmo risco. Datas continuam epoch millis UTC (convenção já usada em Projeto/Etapa).
 */
object DataFormatter {

    fun formatar(epochMillis: Long): String {
        val data = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.UTC).date
        return "${data.dayOfMonth.toString().padStart(2, '0')}/${data.monthNumber.toString().padStart(2, '0')}/${data.year}"
    }

    fun parseOuNulo(texto: String): Long? {
        val partes = texto.trim().split("/")
        if (partes.size != 3) return null
        val dia = partes[0].toIntOrNull() ?: return null
        val mes = partes[1].toIntOrNull() ?: return null
        val ano = partes[2].toIntOrNull() ?: return null
        val data = runCatching { LocalDate(ano, mes, dia) }.getOrNull() ?: return null
        return data.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    }
}
