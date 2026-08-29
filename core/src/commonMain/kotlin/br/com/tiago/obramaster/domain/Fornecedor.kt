package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

/** SPEC_OBRA_MASTER.md §4.4 — extensão 1:1 de uma Pessoa (tag FORNECEDOR), mesmo padrão de Funcionario. */
@Serializable
data class Fornecedor(
    val pessoaId: String,
    val cnpjCpf: String? = null,
    val observacoes: String? = null,
    val ativo: Boolean = true,
)
