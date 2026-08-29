package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
data class Cor(
    val id: String,
    val nome: String,
    val hex: String,
    val codigoFabricante: String? = null,
    val ativo: Boolean = true,
)
