package br.com.tiago.obramaster.domain

data class Abertura(
    val id: String,
    val paredeId: String,
    val tipo: TipoAbertura,
    val posicaoNaParede: Double, // 0.0..1.0
    val larguraCm: Double,
    val ativo: Boolean = true,
)
