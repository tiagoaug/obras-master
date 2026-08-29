package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.ConfigBDI
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.ConfigBDIRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.configBdiRoutes() {
    get("/configs-bdi") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = false) ?: return@get
        call.respond(ConfigBDIRepository.listar(principal.empresaId))
    }

    get("/configs-bdi/{id}") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val config = ConfigBDIRepository.buscarPorId(principal.empresaId, id)
        if (config == null) call.respond(HttpStatusCode.NotFound) else call.respond(config)
    }

    post("/configs-bdi") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = true) ?: return@post
        val config = call.receive<ConfigBDI>()
        call.respond(HttpStatusCode.Created, ConfigBDIRepository.criar(principal.empresaId, config))
    }

    put("/configs-bdi/{id}") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val config = call.receive<ConfigBDI>()
        if (config.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (ConfigBDIRepository.atualizar(principal.empresaId, config)) call.respond(config) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/configs-bdi/{id}") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (ConfigBDIRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
