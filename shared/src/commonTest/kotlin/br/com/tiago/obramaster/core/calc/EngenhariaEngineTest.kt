package br.com.tiago.obramaster.core.calc

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EngenhariaEngineTest {

    private fun assertProximo(esperado: Double, obtido: Double?, tolerancia: Double = 1e-6) {
        assertNotNull(obtido)
        assertEquals(true, abs(esperado - obtido) < tolerancia, "esperado $esperado, obtido $obtido")
    }

    @Test
    fun tracoConcreto_proporcoesRespeitamAsPartesInformadas() {
        val resultado = EngenhariaEngine.tracoConcreto(volumeM3 = 1.0, partesCimento = 1.0, partesAreia = 2.0, partesBrita = 3.0, fatorAguaCimento = 0.5)
        assertNotNull(resultado)
        assertTrue(resultado.cimentoKg > 0)
        assertProximo(2.0 * resultado.cimentoKg, resultado.areiaKg)
        assertProximo(3.0 * resultado.cimentoKg, resultado.britaKg)
        assertProximo(0.5 * resultado.cimentoKg, resultado.aguaLitros)
        assertProximo(resultado.cimentoKg / 50.0, resultado.sacosCimento50kg)
    }

    @Test
    fun tracoConcreto_semBritaRetornaNull() {
        assertNull(EngenhariaEngine.tracoConcreto(1.0, 1.0, 2.0, 0.0, 0.5))
    }

    @Test
    fun tracoConcreto_volumeInvalidoRetornaNull() {
        assertNull(EngenhariaEngine.tracoConcreto(0.0, 1.0, 2.0, 3.0, 0.5))
    }

    @Test
    fun tracoArgamassa_naoTemBrita() {
        val resultado = EngenhariaEngine.tracoArgamassa(volumeM3 = 0.5, partesCimento = 1.0, partesAreia = 4.0, fatorAguaCimento = 0.8)
        assertNotNull(resultado)
        assertNull(resultado.britaKg)
        assertProximo(4.0 * resultado.cimentoKg, resultado.areiaKg)
    }

    @Test
    fun volumeArgamassaPorArea_espessura2cm() {
        assertProximo(0.2, EngenhariaEngine.volumeArgamassaPorArea(areaM2 = 10.0, espessuraCm = 2.0))
    }

    @Test
    fun tijolosPorM2_semJunta() {
        // tijolo 20x10cm sem junta = 0,02 m² por unidade -> 50 unidades/m²
        assertProximo(50.0, EngenhariaEngine.tijolosPorM2(comprimentoCm = 20.0, alturaCm = 10.0, juntaCm = 0.0))
    }

    @Test
    fun tijolosPorM2_comJuntaReduzQuantidade() {
        val semJunta = EngenhariaEngine.tijolosPorM2(20.0, 10.0, 0.0)
        val comJunta = EngenhariaEngine.tijolosPorM2(20.0, 10.0, 1.0)
        assertNotNull(semJunta)
        assertNotNull(comJunta)
        assertTrue(comJunta < semJunta)
    }

    @Test
    fun quantidadeTijolos_aplicaPercentualDePerda() {
        assertProximo(110.0, EngenhariaEngine.quantidadeTijolos(areaParedeM2 = 10.0, tijolosPorM2 = 10.0, percentualPerda = 10.0))
    }

    @Test
    fun caixasNecessarias_arredondamentoFicaPraUiExato() {
        assertProximo(2.2, EngenhariaEngine.caixasNecessarias(areaM2 = 20.0, percentualPerda = 10.0, areaPorCaixaM2 = 10.0))
    }

    @Test
    fun litrosTinta_duasDemaos() {
        assertProximo(10.0, EngenhariaEngine.litrosTinta(areaM2 = 50.0, numeroDemaos = 2.0, rendimentoM2PorLitro = 10.0))
    }

    @Test
    fun latasNecessarias() {
        assertProximo(2.5, EngenhariaEngine.latasNecessarias(litros = 45.0, volumeLataLitros = 18.0))
    }

    @Test
    fun areaInclinadaTelhado_semInclinacaoMantemAreaPlana() {
        assertProximo(100.0, EngenhariaEngine.areaInclinadaTelhado(areaPlanaM2 = 100.0, inclinacaoPercentual = 0.0))
    }

    @Test
    fun areaInclinadaTelhado_inclinacao100PorCentoEquivaleA45Graus() {
        // inclinação 100% = 45°, fator = √2
        assertProximo(100.0 * kotlin.math.sqrt(2.0), EngenhariaEngine.areaInclinadaTelhado(100.0, 100.0))
    }

    @Test
    fun calcularDegraus_alturaTotal280PisoDesejado30() {
        val resultado = EngenhariaEngine.calcularDegraus(alturaTotalCm = 280.0, pisoDesejadoCm = 30.0)
        assertNotNull(resultado)
        assertProximo(2 * resultado.alturaEspelhoCm + resultado.profundidadePisoCm, resultado.valorBlondelCm)
        assertTrue(resultado.dentroDaFaixaRecomendada)
        assertProximo(resultado.alturaEspelhoCm * resultado.numeroDegraus, 280.0)
    }

    @Test
    fun calcularDegraus_entradaInvalidaRetornaNull() {
        assertNull(EngenhariaEngine.calcularDegraus(0.0, 30.0))
        assertNull(EngenhariaEngine.calcularDegraus(280.0, 0.0))
    }

    @Test
    fun kgAcoEstimado_taxaPorM3() {
        assertProximo(800.0, EngenhariaEngine.kgAcoEstimado(volumeConcretoM3 = 10.0, taxaKgPorM3 = 80.0))
    }

    @Test
    fun kgAcoEstimado_entradaInvalidaRetornaNull() {
        assertNull(EngenhariaEngine.kgAcoEstimado(0.0, 80.0))
        assertNull(EngenhariaEngine.kgAcoEstimado(10.0, 0.0))
    }
}
