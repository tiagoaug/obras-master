package br.com.tiago.obramaster.domain

data class Pessoa(
    val id: String,
    val nome: String,
    val tags: Set<TagPessoa>,
    val telefone: String? = null,
    val email: String? = null,
    val endereco: String? = null,
    val documento: String? = null,
    val fotoUri: String? = null,
    val observacoes: String? = null,
    val ativo: Boolean = true,
)
