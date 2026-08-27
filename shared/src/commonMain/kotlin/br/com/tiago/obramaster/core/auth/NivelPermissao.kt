package br.com.tiago.obramaster.core.auth

import kotlinx.serialization.Serializable

@Serializable
enum class NivelPermissao {
    NENHUM, LEITURA, ESCRITA, TOTAL
}
