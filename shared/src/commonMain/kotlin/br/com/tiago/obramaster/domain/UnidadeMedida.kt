package br.com.tiago.obramaster.domain

data class UnidadeMedida(
    val id: String,
    val sigla: String,
    val nome: String,
    val ativo: Boolean = true,
)
