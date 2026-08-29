package br.com.tiago.obramaster.domain

import br.com.tiago.obramaster.core.auth.NivelPermissao
import kotlinx.serialization.Serializable

/** Fase 10 (pivô Firebase) — registro pendente criado pelo Gestor ao convidar alguém (ver
 * Colaborador.kt). Sem senha, sem uid ainda: vira um Colaborador de verdade quando a pessoa
 * convidada entra pela primeira vez (Google ou e-mail/senha própria) usando este `email`. */
@Serializable
data class ConviteColaborador(
    val id: String,
    val empresaId: String,
    val email: String,
    val nome: String,
    val ehGestor: Boolean = false,
    val permissoes: Map<String, NivelPermissao> = emptyMap(), // moduleId -> nível
    val criadoEm: Long,
)
