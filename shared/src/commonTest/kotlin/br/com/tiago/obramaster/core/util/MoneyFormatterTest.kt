package br.com.tiago.obramaster.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {

    @Test
    fun formataValoresSimples() {
        assertEquals("R$ 0,00", MoneyFormatter.formatar(0))
        assertEquals("R$ 1,23", MoneyFormatter.formatar(123))
        assertEquals("R$ 10,00", MoneyFormatter.formatar(1000))
    }

    @Test
    fun agrupaMilhares() {
        assertEquals("R$ 1.234,56", MoneyFormatter.formatar(123_456))
        assertEquals("R$ 1.234.567,89", MoneyFormatter.formatar(123_456_789))
    }

    @Test
    fun formataValoresNegativos() {
        assertEquals("-R$ 1,23", MoneyFormatter.formatar(-123))
    }
}
