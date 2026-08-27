package br.com.tiago.obramaster.domain

data class Etapa(
    val id: String,
    val projetoId: String,
    val nome: String,
    val ordem: Int,
    val orcamentoEtapa: Long, // centavos
    val dataInicio: Long? = null,
    val dataFim: Long? = null,
    val progressoPercent: Int = 0,
    val status: StatusEtapa = StatusEtapa.NAO_INICIADA,
    val ativo: Boolean = true,
)
