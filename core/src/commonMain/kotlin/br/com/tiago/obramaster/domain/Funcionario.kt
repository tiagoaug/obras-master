package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TipoContratacao { DIARIA, EMPREITADA, MENSAL }

/** SPEC_OBRA_MASTER.md §4.3 — extensão 1:1 de uma Pessoa (tag FUNCIONARIO); a chave é o próprio pessoaId. */
@Serializable
data class Funcionario(
    val pessoaId: String,
    val funcao: String,
    val tipoContratacao: TipoContratacao,
    val valorBase: Long, // centavos
    val ativo: Boolean = true,
)
