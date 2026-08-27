package br.com.tiago.obramaster.domain

data class Cor(
    val id: String,
    val nome: String,
    val hex: String,
    val codigoFabricante: String? = null,
    val ativo: Boolean = true,
)
