package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class StatusProjeto { PLANEJAMENTO, EM_EXECUCAO, PAUSADO, CONCLUIDO }
