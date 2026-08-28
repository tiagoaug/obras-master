package br.com.tiago.obramaster.core.orcamentos

import br.com.tiago.obramaster.domain.ConfigBDI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BdiEngineTest {

    private fun config(
        administracaoCentral: Double = 0.0,
        seguroGarantia: Double = 0.0,
        riscos: Double = 0.0,
        despesasFinanceiras: Double = 0.0,
        lucro: Double = 0.0,
        tributos: Double = 0.0,
    ) = ConfigBDI(
        id = "c1", nome = "teste",
        administracaoCentral = administracaoCentral,
        seguroGarantia = seguroGarantia,
        riscos = riscos,
        despesasFinanceiras = despesasFinanceiras,
        lucro = lucro,
        tributos = tributos,
    )

    /** SPEC_OBRA_MASTER_ADENDO_BDI.md §2.2 — AC=4%, S=0.8%, R=1%, DF=1%, L=10%, I=5.65% → BDI = 24.52%. */
    @Test
    fun calcularBdi_batComExemploNumericoDaSpec() {
        val bdi = BdiEngine.calcularBdi(
            config(
                administracaoCentral = 0.04,
                seguroGarantia = 0.008,
                riscos = 0.01,
                despesasFinanceiras = 0.01,
                lucro = 0.10,
                tributos = 0.0565,
            ),
        )
        assertTrue(abs(bdi - 0.2452) < 0.0001, "BDI esperado ~0.2452, obtido $bdi")
    }

    @Test
    fun calcularBdi_todasAsTaxasZero_resultaEmBdiZero() {
        assertEquals(0.0, BdiEngine.calcularBdi(config()))
    }

    @Test
    fun calcularBdi_semTributos_ignoraGrossUp() {
        val bdi = BdiEngine.calcularBdi(config(lucro = 0.10, tributos = 0.0))
        assertTrue(abs(bdi - 0.10) < 0.0001)
    }

    @Test
    fun calcularBdi_semLucro_naoAdicionaMargem() {
        val bdi = BdiEngine.calcularBdi(config(administracaoCentral = 0.05, lucro = 0.0))
        assertTrue(abs(bdi - 0.05) < 0.0001)
    }

    @Test
    fun aplicarBdi_calculaPrecoDeVendaSobreCustoDireto() {
        val custoDireto = 100_000_00L
        val configTeste = config(
            administracaoCentral = 0.04,
            seguroGarantia = 0.008,
            riscos = 0.01,
            despesasFinanceiras = 0.01,
            lucro = 0.10,
            tributos = 0.0565,
        )
        val resultado = BdiEngine.aplicarBdi(custoDireto, configTeste)

        // O exemplo da spec (§2.2) arredonda o BDI pra 24,52% ao exibir; o valor exato da fração é
        // ~24,5152%, então o preço de venda batido contra a fórmula difere de "R$124.520" em centavos.
        assertTrue(abs(resultado.bdiPercentual - 0.2452) < 0.0001)
        val precoEsperado = (custoDireto * (1 + resultado.bdiPercentual)).toLong()
        assertEquals(precoEsperado, resultado.precoVenda)
    }
}
