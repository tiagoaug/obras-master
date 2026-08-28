package br.com.tiago.obramaster.domain

/** SPEC_OBRA_MASTER.md §4.4 — extensão 1:1 de uma Pessoa (tag FORNECEDOR), mesmo padrão de Funcionario. */
data class Fornecedor(
    val pessoaId: String,
    val cnpjCpf: String? = null,
    val observacoes: String? = null,
    val ativo: Boolean = true,
)
