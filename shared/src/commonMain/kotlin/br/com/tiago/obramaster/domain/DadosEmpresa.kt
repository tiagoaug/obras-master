package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
data class DadosEmpresa(
    val id: String,
    val nome: String,
    val logoUri: String? = null,
    val cnpj: String? = null,
    val telefone: String? = null,
    val endereco: String? = null,
    val cidade: String? = null,
)
