package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

/** SPEC_OBRA_MASTER.md §4.3 — membrosIds junta a tabela EquipeMembro no agregado, mesmo padrão de Pessoa.tags. */
@Serializable
data class Equipe(
    val id: String,
    val nome: String,
    val liderPessoaId: String? = null,
    val membrosIds: Set<String> = emptySet(),
    val ativo: Boolean = true,
)
