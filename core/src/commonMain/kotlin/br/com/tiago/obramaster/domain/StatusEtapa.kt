package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class StatusEtapa { NAO_INICIADA, EM_ANDAMENTO, PAUSADA, CONCLUIDA }
