package br.com.tiago.obramaster.domain

import br.com.tiago.obramaster.core.auth.NivelPermissao
import kotlinx.serialization.Serializable

@Serializable
data class Permissao(
    val colaboradorId: String,
    val moduleId: String,
    val nivel: NivelPermissao,
)
