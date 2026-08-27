package br.com.tiago.obramaster.core.onboarding

import kotlinx.serialization.Serializable

/**
 * O que o usuário escolheu nas etapas Categorias/BDI/Template de Etapas/Primeiro Projeto,
 * que ainda não têm tabela própria (chegam nas Fases 3, 4 e 6). Fica salvo aqui até essas
 * fases existirem e lerem/aplicarem essas escolhas.
 */
@Serializable
data class PreferenciasPosOnboarding(
    val usarCategoriasDefault: Boolean,
    val usarBdiPadrao: Boolean,
    val usarTemplateEtapasPadrao: Boolean,
    val primeiroProjeto: ProjetoDraft?,
)
