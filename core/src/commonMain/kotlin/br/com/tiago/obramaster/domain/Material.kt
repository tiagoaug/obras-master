package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: String,
    val nome: String,
    val unidadePadrao: String,
    val precoReferencia: Long? = null, // centavos
    val categoria: String? = null,
    val corId: String? = null,
    val ativo: Boolean = true,
)
