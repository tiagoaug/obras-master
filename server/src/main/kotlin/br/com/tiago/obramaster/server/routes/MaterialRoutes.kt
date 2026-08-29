package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.MaterialRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.materialRoutes() {
    get("/materiais") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = false) ?: return@get
        call.respond(MaterialRepository.listar(principal.empresaId))
    }

    get("/materiais/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val material = MaterialRepository.buscarPorId(principal.empresaId, id)
        if (material == null) call.respond(HttpStatusCode.NotFound) else call.respond(material)
    }

    post("/materiais") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@post
        val material = call.receive<Material>()
        call.respond(HttpStatusCode.Created, MaterialRepository.criar(principal.empresaId, material))
    }

    put("/materiais/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val material = call.receive<Material>()
        if (material.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (MaterialRepository.atualizar(principal.empresaId, material)) call.respond(material) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/materiais/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (MaterialRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
