package br.com.tiago.obramaster.domain

data class Parede(
    val id: String,
    val plantaId: String,
    val pontoInicio: PontoXY,
    val pontoFim: PontoXY,
    val espessuraCm: Double = 15.0,
    val estrutural: Boolean = false,
    val ativo: Boolean = true,
)
