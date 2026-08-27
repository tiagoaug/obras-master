package br.com.tiago.obramaster.domain

import br.com.tiago.obramaster.core.auth.NivelPermissao

data class Permissao(
    val colaboradorId: String,
    val moduleId: String,
    val nivel: NivelPermissao,
)
