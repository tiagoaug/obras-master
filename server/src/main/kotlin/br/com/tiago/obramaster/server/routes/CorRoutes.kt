package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Cor
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.CorRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.corRoutes() {
    get("/cores") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = false) ?: return@get
        call.respond(CorRepository.listar(principal.empresaId))
    }

    get("/cores/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val cor = CorRepository.buscarPorId(principal.empresaId, id)
        if (cor == null) call.respond(HttpStatusCode.NotFound) else call.respond(cor)
    }

    post("/cores") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@post
        val cor = call.receive<Cor>()
        call.respond(HttpStatusCode.Created, CorRepository.criar(principal.empresaId, cor))
    }

    put("/cores/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val cor = call.receive<Cor>()
        if (cor.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (CorRepository.atualizar(principal.empresaId, cor)) call.respond(cor) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/cores/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (CorRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
