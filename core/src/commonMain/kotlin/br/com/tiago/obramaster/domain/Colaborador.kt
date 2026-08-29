package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

/** Fase 10 (pivô Firebase) — `id` é o `uid` do Firebase Auth; a senha não é mais guardada aqui
 * (Firebase Auth cuida disso). Ver ConviteColaborador para o fluxo de "Gestor convida colaborador
 * por e-mail" — o Firebase Auth do lado do cliente não permite criar a conta de outra pessoa sem
 * deslogar quem está criando, então o colaborador só vira um Colaborador de verdade quando ele
 * mesmo aceita o convite e entra pela primeira vez. */
@Serializable
data class Colaborador(
    val id: String,
    val nome: String,
    val email: String,
    val ativo: Boolean,
    val ehGestor: Boolean,
)
