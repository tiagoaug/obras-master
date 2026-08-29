package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Equipe
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.EquipeRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.equipeRoutes() {
    get("/equipes") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        call.respond(EquipeRepository.listar(principal.empresaId))
    }

    get("/equipes/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val equipe = EquipeRepository.buscarPorId(principal.empresaId, id)
        if (equipe == null) call.respond(HttpStatusCode.NotFound) else call.respond(equipe)
    }

    post("/equipes") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@post
        val equipe = call.receive<Equipe>()
        call.respond(HttpStatusCode.Created, EquipeRepository.criar(principal.empresaId, equipe))
    }

    put("/equipes/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val equipe = call.receive<Equipe>()
        if (equipe.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (EquipeRepository.atualizar(principal.empresaId, equipe)) call.respond(equipe) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/equipes/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (EquipeRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
