package br.com.tiago.obramaster.core.assistant

import br.com.tiago.obramaster.core.modules.AppModule
import kotlinx.serialization.Serializable

/** SPEC_ASSISTENTE_IA.md §2.1 — corresponde 1:1 ao formato de `docs/manual_index.json`, a fonte
 * de conhecimento do Assistente (bundlada como asset no app, ver ManualRepository em :shared). */
@Serializable
data class ManualSection(
    val id: String,
    val modulo: AppModule? = null,
    val titulo: String,
    val conteudo: String,
    val exemploPratico: String? = null,
    val palavrasChave: List<String> = emptyList(),
)

@Serializable
data class ManualIndex(
    val versaoManual: String,
    val geradoEm: String,
    val secoes: List<ManualSection>,
)
