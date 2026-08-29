package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.MetaRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.metaRoutes() {
    get("/metas") {
        val principal = call.autorizarOuResponder(AppModule.METAS, exigirEscrita = false) ?: return@get
        call.respond(MetaRepository.listar(principal.empresaId))
    }

    get("/metas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.METAS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val meta = MetaRepository.buscarPorId(principal.empresaId, id)
        if (meta == null) call.respond(HttpStatusCode.NotFound) else call.respond(meta)
    }

    post("/metas") {
        val principal = call.autorizarOuResponder(AppModule.METAS, exigirEscrita = true) ?: return@post
        val meta = call.receive<Meta>()
        call.respond(HttpStatusCode.Created, MetaRepository.criar(principal.empresaId, meta))
    }

    put("/metas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.METAS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val meta = call.receive<Meta>()
        if (meta.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (MetaRepository.atualizar(principal.empresaId, meta)) call.respond(meta) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/metas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.METAS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (MetaRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
