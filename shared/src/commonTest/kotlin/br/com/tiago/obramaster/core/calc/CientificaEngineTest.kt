package br.com.tiago.obramaster.core.calc

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CientificaEngineTest {

    private fun assertProximo(esperado: Double, obtido: Double?, tolerancia: Double = 1e-9) {
        assertNotNull(obtido)
        assertEquals(true, abs(esperado - obtido) < tolerancia, "esperado $esperado, obtido $obtido")
    }

    @Test
    fun potencia_casosBasicos() {
        assertProximo(8.0, CientificaEngine.potencia(2.0, 3.0))
        assertProximo(1.0, CientificaEngine.potencia(5.0, 0.0))
        assertProximo(0.25, CientificaEngine.potencia(2.0, -2.0))
    }

    @Test
    fun raizQuadrada_negativoRetornaNull() {
        assertNull(CientificaEngine.raizQuadrada(-4.0))
        assertProximo(3.0, CientificaEngine.raizQuadrada(9.0))
    }

    @Test
    fun raizN_indiceImparDeNegativoFunciona() {
        assertProximo(-2.0, CientificaEngine.raizN(-8.0, 3.0))
    }

    @Test
    fun raizN_indiceParDeNegativoRetornaNull() {
        assertNull(CientificaEngine.raizN(-8.0, 2.0))
    }

    @Test
    fun log10_zeroOuNegativoRetornaNull() {
        assertNull(CientificaEngine.log10(0.0))
        assertNull(CientificaEngine.log10(-1.0))
        assertProximo(2.0, CientificaEngine.log10(100.0))
    }

    @Test
    fun ln_zeroOuNegativoRetornaNull() {
        assertNull(CientificaEngine.ln(0.0))
        assertNull(CientificaEngine.ln(-1.0))
    }

    @Test
    fun fatorial_casosBasicos() {
        assertProximo(1.0, CientificaEngine.fatorial(0))
        assertProximo(120.0, CientificaEngine.fatorial(5))
        assertNull(CientificaEngine.fatorial(-1))
    }

    @Test
    fun fatorial_muitoGrandeEstouraDoubleRetornaNull() {
        assertNull(CientificaEngine.fatorial(200))
    }

    @Test
    fun percentual_dividePorCem() {
        assertProximo(0.5, CientificaEngine.percentual(50.0))
    }
}
