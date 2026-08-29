package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.domain.Permissao
import br.com.tiago.obramaster.server.auth.requestPrincipal
import br.com.tiago.obramaster.server.repository.ColaboradorRepository
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class PermissoesResponse(val ehGestor: Boolean, val permissoes: List<Permissao>)

fun Route.meRoutes() {
    get("/me/permissions") {
        val principal = call.requestPrincipal()
        call.respond(
            PermissoesResponse(
                ehGestor = principal.ehGestor,
                permissoes = ColaboradorRepository.permissoesDoColaborador(principal.colaboradorId),
            ),
        )
    }
}
