package br.com.tiago.obramaster.core.calc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArithmeticEvaluatorTest {

    @Test
    fun operacoesBasicas() {
        assertEquals(7.0, ArithmeticEvaluator.avaliar("3+4"))
        assertEquals(-1.0, ArithmeticEvaluator.avaliar("3-4"))
        assertEquals(12.0, ArithmeticEvaluator.avaliar("3*4"))
        assertEquals(2.0, ArithmeticEvaluator.avaliar("8/4"))
    }

    @Test
    fun precedenciaEParenteses() {
        assertEquals(14.0, ArithmeticEvaluator.avaliar("2+3*4"))
        assertEquals(20.0, ArithmeticEvaluator.avaliar("(2+3)*4"))
    }

    @Test
    fun percentualDividePor100() {
        assertEquals(0.1, ArithmeticEvaluator.avaliar("10%"))
        assertEquals(200.1, ArithmeticEvaluator.avaliar("200+10%"))
    }

    @Test
    fun simbolosDeMultiplicacaoEDivisao() {
        assertEquals(12.0, ArithmeticEvaluator.avaliar("3×4"))
        assertEquals(2.0, ArithmeticEvaluator.avaliar("8÷4"))
    }

    @Test
    fun divisaoPorZeroRetornaNull() {
        assertNull(ArithmeticEvaluator.avaliar("5/0"))
    }

    @Test
    fun expressaoInvalidaRetornaNull() {
        assertNull(ArithmeticEvaluator.avaliar("3+"))
        assertNull(ArithmeticEvaluator.avaliar("(2+3"))
        assertNull(ArithmeticEvaluator.avaliar("abc"))
    }
}
