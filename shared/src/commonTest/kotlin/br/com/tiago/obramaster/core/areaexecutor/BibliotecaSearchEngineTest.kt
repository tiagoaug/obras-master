package br.com.tiago.obramaster.core.areaexecutor

import br.com.tiago.obramaster.domain.CategoriaNorma
import br.com.tiago.obramaster.domain.NormaABNT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BibliotecaSearchEngineTest {

    private val normas = listOf(
        NormaABNT(
            numero = "NBR 6118", titulo = "Projeto de Estruturas de Concreto",
            categoria = CategoriaNorma.ESTRUTURA, escopoResumo = "resumo", urlCatalogoOficial = "https://exemplo.com",
        ),
        NormaABNT(
            numero = "NBR 5410", titulo = "Instalações Elétricas de Baixa Tensão",
            categoria = CategoriaNorma.ELETRICA, escopoResumo = "resumo", urlCatalogoOficial = "https://exemplo.com",
        ),
        NormaABNT(
            numero = "NBR 9050", titulo = "Acessibilidade a Edificações",
            categoria = CategoriaNorma.ACESSIBILIDADE, escopoResumo = "resumo", urlCatalogoOficial = "https://exemplo.com",
        ),
    )

    @Test
    fun buscarNormas_queryVaziaRetornaTodas() {
        assertEquals(3, BibliotecaSearchEngine.buscarNormas("", normas).size)
    }

    @Test
    fun buscarNormas_porNumeroParcial() {
        val resultado = BibliotecaSearchEngine.buscarNormas("6118", normas)
        assertEquals(1, resultado.size)
        assertEquals("NBR 6118", resultado.first().numero)
    }

    @Test
    fun buscarNormas_porTituloIgnorandoCase() {
        val resultado = BibliotecaSearchEngine.buscarNormas("elétricas", normas)
        assertEquals(1, resultado.size)
        assertEquals("NBR 5410", resultado.first().numero)
    }

    @Test
    fun buscarNormas_porCategoria() {
        val resultado = BibliotecaSearchEngine.buscarNormas("acessibilidade", normas)
        assertEquals(1, resultado.size)
        assertEquals("NBR 9050", resultado.first().numero)
    }

    @Test
    fun buscarNormas_semResultado() {
        assertTrue(BibliotecaSearchEngine.buscarNormas("hidráulica", normas).isEmpty())
    }
}
