package br.com.tiago.obramaster.core.onboarding

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.ui.theme.PrefsAcessibilidade
import kotlinx.serialization.Serializable

/** SPEC_ONBOARDING.md §5.1 — evita sobrecarregar quem está começando. */
val SUGESTAO_INICIAL_MODULOS = setOf(
    AppModule.PROJETOS, AppModule.FINANCEIRO, AppModule.EQUIPES,
    AppModule.COMPRAS, AppModule.CADASTROS_BASE, AppModule.PESSOAS,
    AppModule.CALCULADORAS, AppModule.RELATORIOS,
)

@Serializable
data class OnboardingState(
    val etapaAtual: OnboardingStep = OnboardingStep.BOAS_VINDAS,
    val empresa: DadosEmpresaDraft = DadosEmpresaDraft(),
    val gestor: GestorDraft = GestorDraft(),
    val modulosAtivos: Set<AppModule> = SUGESTAO_INICIAL_MODULOS,
    val contas: List<ContaDraft> = emptyList(),
    val usarCategoriasDefault: Boolean = true,
    val usarBdiPadrao: Boolean = true,
    val usarTemplateEtapasPadrao: Boolean = true,
    val colaboradores: List<ColaboradorDraft> = emptyList(),
    val primeiroProjeto: ProjetoDraft? = null,
    val acessibilidade: PrefsAcessibilidade = PrefsAcessibilidade(),
    val etapasConcluidas: Set<OnboardingStep> = emptySet(),
)
