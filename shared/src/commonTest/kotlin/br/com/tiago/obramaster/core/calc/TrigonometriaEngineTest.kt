package br.com.tiago.obramaster.core.calc

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TrigonometriaEngineTest {

    private fun assertProximo(esperado: Double, obtido: Double?, tolerancia: Double = 1e-6) {
        assertNotNull(obtido)
        assertEquals(true, abs(esperado - obtido) < tolerancia, "esperado $esperado, obtido $obtido")
    }

    @Test
    fun senoCossenoTangente_angulosNotaveis() {
        assertProximo(1.0, TrigonometriaEngine.seno(90.0))
        assertProximo(1.0, TrigonometriaEngine.cosseno(0.0))
        assertProximo(1.0, TrigonometriaEngine.tangente(45.0))
    }

    @Test
    fun tangente_90GrausRetornaNull() {
        assertNull(TrigonometriaEngine.tangente(90.0))
    }

    @Test
    fun arcoSeno_foraDoDominioRetornaNull() {
        assertNull(TrigonometriaEngine.arcoSeno(1.5))
        assertProximo(90.0, TrigonometriaEngine.arcoSeno(1.0))
    }

    @Test
    fun arcoCosseno_foraDoDominioRetornaNull() {
        assertNull(TrigonometriaEngine.arcoCosseno(-1.5))
    }

    @Test
    fun pitagorasHipotenusa_triangulo345() {
        assertProximo(5.0, TrigonometriaEngine.pitagorasHipotenusa(3.0, 4.0))
    }

    @Test
    fun pitagorasCateto_catetoMaiorQueHipotenusaRetornaNull() {
        assertNull(TrigonometriaEngine.pitagorasCateto(5.0, 6.0))
    }

    @Test
    fun leiDosCossenos_anguloRetoEquivaleAPitagoras() {
        assertProximo(5.0, TrigonometriaEngine.leiDosCossenos(3.0, 4.0, 90.0))
    }

    @Test
    fun leiDosSenos_triangulo306090() {
        // lado oposto a 30° = 1, oposto a 60° = √3, oposto a 90° = 2 (proporção clássica)
        assertProximo(sqrt(3.0), TrigonometriaEngine.leiDosSenos(1.0, 30.0, 60.0))
        assertProximo(2.0, TrigonometriaEngine.leiDosSenos(1.0, 30.0, 90.0))
    }

    @Test
    fun resolverTrianguloLLL_triangulo345_anguloRetoOpostoAoMaiorLado() {
        val resultado = TrigonometriaEngine.resolverTrianguloLLL(3.0, 4.0, 5.0)
        assertNotNull(resultado)
        assertProximo(90.0, resultado.anguloC)
    }

    @Test
    fun resolverTrianguloLLL_desigualdadeTriangularInvalidaRetornaNull() {
        assertNull(TrigonometriaEngine.resolverTrianguloLLL(1.0, 1.0, 10.0))
    }

    @Test
    fun resolverTrianguloLAL_anguloRetoGeraTriangulo345() {
        val resultado = TrigonometriaEngine.resolverTrianguloLAL(3.0, 4.0, 90.0)
        assertNotNull(resultado)
        assertProximo(5.0, resultado.ladoC)
    }

    @Test
    fun resolverTrianguloAAL_triangulo306090() {
        val resultado = TrigonometriaEngine.resolverTrianguloAAL(1.0, 30.0, 60.0)
        assertNotNull(resultado)
        assertProximo(sqrt(3.0), resultado.ladoB)
        assertProximo(2.0, resultado.ladoC)
        assertProximo(90.0, resultado.anguloC)
    }

    @Test
    fun resolverTrianguloAAL_somaDeAngulosMaiorQue180RetornaNull() {
        assertNull(TrigonometriaEngine.resolverTrianguloAAL(1.0, 100.0, 100.0))
    }
}
