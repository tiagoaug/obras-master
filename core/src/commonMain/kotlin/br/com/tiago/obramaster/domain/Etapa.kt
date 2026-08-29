package br.com.tiago.obramaster.domain

/** dataInicio/dataFim: datas PREVISTAS do cronograma. dataInicioReal/dataFimReal: adição técnica
 * sinalizada (Fase 7.1) — SPEC_OBRA_MASTER.md §4.7 pede "datas previstas vs reais" no cronograma,
 * mas só havia um par de datas; o par original passou a significar "previstas". */
data class Etapa(
    val id: String,
    val projetoId: String,
    val nome: String,
    val ordem: Int,
    val orcamentoEtapa: Long, // centavos
    val dataInicio: Long? = null,
    val dataFim: Long? = null,
    val dataInicioReal: Long? = null,
    val dataFimReal: Long? = null,
    val progressoPercent: Int = 0,
    val status: StatusEtapa = StatusEtapa.NAO_INICIADA,
    val ativo: Boolean = true,
)
