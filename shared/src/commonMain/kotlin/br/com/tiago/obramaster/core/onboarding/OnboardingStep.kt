package br.com.tiago.obramaster.core.onboarding

import kotlinx.serialization.Serializable

/** SPEC_ONBOARDING.md §2/§5 — ordem fixa das 12 etapas. */
@Serializable
enum class OnboardingStep {
    BOAS_VINDAS, EMPRESA, GESTOR, MODULOS, CONTAS_FINANCEIRAS,
    CATEGORIAS, BDI, TEMPLATE_ETAPAS, COLABORADORES,
    PRIMEIRO_PROJETO, ACESSIBILIDADE, RESUMO;

    /** §2 — "só o mínimo para o app funcionar é obrigatório". */
    val obrigatoria: Boolean
        get() = this == EMPRESA || this == GESTOR || this == MODULOS || this == CONTAS_FINANCEIRAS

    fun proxima(): OnboardingStep? = entries.getOrNull(ordinal + 1)
    fun anterior(): OnboardingStep? = entries.getOrNull(ordinal - 1)
}
