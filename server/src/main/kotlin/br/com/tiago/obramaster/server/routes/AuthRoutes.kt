package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.server.auth.JwtService
import br.com.tiago.obramaster.server.auth.TipoToken
import br.com.tiago.obramaster.server.auth.requestPrincipal
import br.com.tiago.obramaster.server.repository.ColaboradorRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val login: String, val senha: String, val empresaId: String)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val colaboradorId: String,
    val nome: String,
    val ehGestor: Boolean,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

/** SPEC_OBRA_MASTER_KMP.md §6.2 — POST /auth/login → JWT (access + refresh). O `empresaId` vem no
 * corpo do login porque multi-tenant: o mesmo login pode existir em empresas diferentes. */
fun Route.authRoutes() {
    post("/auth/login") {
        val req = call.receive<LoginRequest>()
        val encontrado = ColaboradorRepository.buscarPorLoginEEmpresaComSenha(req.login, req.empresaId)
        val colaborador = encontrado?.first
        val senhaOk = encontrado != null && PasswordHasher.verify(req.senha, encontrado.second.second, encontrado.second.first)
        if (colaborador == null || !senhaOk) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("erro" to "Login ou senha inválidos"))
            return@post
        }
        call.respond(
            TokenResponse(
                accessToken = JwtService.gerarToken(colaborador.id, req.empresaId, colaborador.ehGestor, TipoToken.ACCESS),
                refreshToken = JwtService.gerarToken(colaborador.id, req.empresaId, colaborador.ehGestor, TipoToken.REFRESH),
                colaboradorId = colaborador.id,
                nome = colaborador.nome,
                ehGestor = colaborador.ehGestor,
            ),
        )
    }

    authenticate("auth-jwt-refresh") {
        post("/auth/refresh") {
            val principal = call.requestPrincipal()
            val colaborador = ColaboradorRepository.buscarPorId(principal.colaboradorId)
            if (colaborador == null || !colaborador.ativo) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("erro" to "Colaborador inválido ou inativo"))
                return@post
            }
            call.respond(
                TokenResponse(
                    accessToken = JwtService.gerarToken(colaborador.id, principal.empresaId, colaborador.ehGestor, TipoToken.ACCESS),
                    refreshToken = JwtService.gerarToken(colaborador.id, principal.empresaId, colaborador.ehGestor, TipoToken.REFRESH),
                    colaboradorId = colaborador.id,
                    nome = colaborador.nome,
                    ehGestor = colaborador.ehGestor,
                ),
            )
        }
    }
}
