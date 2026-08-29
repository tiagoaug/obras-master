package br.com.tiago.obramaster.core.assistant

import br.com.tiago.obramaster.core.modules.AppModule

/** SPEC_ASSISTENTE_IA.md §3 — o que cada tela expõe pro Assistente saber "onde o usuário está".
 * `entidadeAberta` carrega só campos-chave já resumidos (nunca a lista completa de dados
 * sensíveis) — ver regra de privacidade §7.3/§7.5 da spec. */
data class TelaContexto(
    val modulo: AppModule?,
    val telaId: String,
    val entidadeAberta: EntidadeResumo? = null,
    val filtrosAtivos: Map<String, String> = emptyMap(),
)

data class EntidadeResumo(
    val tipo: String,
    val id: String,
    val camposChave: Map<String, String>,
)
