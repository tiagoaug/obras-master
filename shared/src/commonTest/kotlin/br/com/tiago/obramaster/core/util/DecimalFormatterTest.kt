package br.com.tiago.obramaster.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalFormatterTest {

    @Test
    fun formataDuasCasasDecimais() {
        assertEquals("1,23", DecimalFormatter.formatar(1.234, 2))
        assertEquals("10,00", DecimalFormatter.formatar(10.0, 2))
    }

    @Test
    fun arredondaCorretamente() {
        assertEquals("1,24", DecimalFormatter.formatar(1.235, 2))
    }

    @Test
    fun formataNegativos() {
        assertEquals("-1,50", DecimalFormatter.formatar(-1.5, 2))
    }

    @Test
    fun formataSemCasasDecimais() {
        assertEquals("42", DecimalFormatter.formatar(42.4, 0))
    }
}
