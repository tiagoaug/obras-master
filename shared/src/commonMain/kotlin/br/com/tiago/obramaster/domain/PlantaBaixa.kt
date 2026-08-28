package br.com.tiago.obramaster.domain

data class PlantaBaixa(
    val id: String,
    val projetoId: String,
    val nome: String,
    val escalaPxPorMetro: Double,
    val imagemFundoKey: String? = null,
    val imagemFundoOpacidade: Float = 0.5f,
    val criadaEm: Long,
    val atualizadaEm: Long,
    val ativo: Boolean = true,
)
