package br.com.tiago.obramaster.domain

data class Comodo(
    val id: String,
    val plantaId: String,
    val nome: String,
    val pontos: List<PontoXY>,
    val corPreenchimento: String,
    val areaM2: Double,
    val perimetroM: Double,
    val ativo: Boolean = true,
)
