package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
data class Projeto(
    val id: String,
    val nome: String,
    val clienteId: String? = null,
    val endereco: String? = null,
    val areaConstruidaM2: Double? = null,
    val areaTerrenoM2: Double? = null,
    val orcamentoTotal: Long, // centavos
    val dataInicio: Long? = null, // epoch millis UTC
    val dataPrevisaoFim: Long? = null,
    val status: StatusProjeto = StatusProjeto.PLANEJAMENTO,
    val fotoCapaUri: String? = null,
    val ativo: Boolean = true,
)
