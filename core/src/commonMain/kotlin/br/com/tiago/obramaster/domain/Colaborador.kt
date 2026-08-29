package br.com.tiago.obramaster.domain

data class Colaborador(
    val id: String,
    val nome: String,
    val login: String,
    val senhaHash: String,
    val salt: String,
    val ativo: Boolean,
    val ehGestor: Boolean,
)
