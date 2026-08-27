package br.com.tiago.obramaster.core.onboarding

import br.com.tiago.obramaster.domain.TipoConta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingEngineTest {

    @Test
    fun avancar_naoPassaDeEtapaObrigatoriaIncompleta() {
        val estado = OnboardingState(etapaAtual = OnboardingStep.EMPRESA)
        val resultado = OnboardingEngine.avancar(estado)
        assertEquals(OnboardingStep.EMPRESA, resultado.etapaAtual)
    }

    @Test
    fun avancar_passaQuandoEtapaObrigatoriaPreenchida() {
        val estado = OnboardingState(
            etapaAtual = OnboardingStep.EMPRESA,
            empresa = DadosEmpresaDraft(nome = "Construtora Silva"),
        )
        val resultado = OnboardingEngine.avancar(estado)
        assertEquals(OnboardingStep.GESTOR, resultado.etapaAtual)
        assertTrue(OnboardingStep.EMPRESA in resultado.etapasConcluidas)
    }

    @Test
    fun pular_naoFuncionaEmEtapaObrigatoria() {
        val estado = OnboardingState(etapaAtual = OnboardingStep.EMPRESA)
        val resultado = OnboardingEngine.pular(estado)
        assertEquals(OnboardingStep.EMPRESA, resultado.etapaAtual)
    }

    @Test
    fun pular_funcionaEmEtapaOpcional() {
        val estado = OnboardingState(etapaAtual = OnboardingStep.CATEGORIAS)
        val resultado = OnboardingEngine.pular(estado)
        assertEquals(OnboardingStep.BDI, resultado.etapaAtual)
    }

    @Test
    fun voltar_naoPassaDaPrimeiraEtapa() {
        val estado = OnboardingState(etapaAtual = OnboardingStep.BOAS_VINDAS)
        val resultado = OnboardingEngine.voltar(estado)
        assertEquals(OnboardingStep.BOAS_VINDAS, resultado.etapaAtual)
    }

    @Test
    fun podeConcluir_falsoSemOMinimoObrigatorio() {
        val estado = OnboardingState()
        assertFalse(OnboardingEngine.podeConcluir(estado))
    }

    @Test
    fun podeConcluir_verdadeiroComOMinimoPreenchidoEOptativosPulados() {
        val estado = OnboardingState(
            empresa = DadosEmpresaDraft(nome = "Construtora Silva"),
            gestor = GestorDraft(nome = "Tiago", login = "tiago", senha = "123456"),
            modulosAtivos = setOf(br.com.tiago.obramaster.core.modules.AppModule.FINANCEIRO),
            contas = listOf(ContaDraft(nome = "Caixa", tipo = TipoConta.CAIXA, saldoInicialCentavos = 0)),
        )
        assertTrue(OnboardingEngine.podeConcluir(estado))
    }
}
