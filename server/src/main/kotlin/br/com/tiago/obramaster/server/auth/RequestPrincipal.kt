package br.com.tiago.obramaster.server.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

data class RequestPrincipal(val colaboradorId: String, val empresaId: String, val ehGestor: Boolean)

fun ApplicationCall.requestPrincipal(): RequestPrincipal {
    val jwt = principal<JWTPrincipal>() ?: error("Rota protegida sem autenticação instalada")
    return RequestPrincipal(
        colaboradorId = jwt.subject ?: error("Token sem subject"),
        empresaId = jwt.getClaim("empresaId", String::class) ?: error("Token sem empresaId"),
        ehGestor = jwt.getClaim("ehGestor", Boolean::class) ?: false,
    )
}
