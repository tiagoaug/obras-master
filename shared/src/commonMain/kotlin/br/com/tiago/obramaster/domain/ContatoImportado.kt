package br.com.tiago.obramaster.domain

data class ContatoImportado(
    val nome: String,
    val telefone: String? = null,
    val email: String? = null,
    val fotoUri: String? = null,
)
