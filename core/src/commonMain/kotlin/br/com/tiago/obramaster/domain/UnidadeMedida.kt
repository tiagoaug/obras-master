package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
data class UnidadeMedida(
    val id: String,
    val sigla: String,
    val nome: String,
    val ativo: Boolean = true,
)
