package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.UnidadeMedida
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.UnidadeMedidaRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.unidadeMedidaRoutes() {
    get("/unidades-medida") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = false) ?: return@get
        call.respond(UnidadeMedidaRepository.listar(principal.empresaId))
    }

    get("/unidades-medida/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val unidade = UnidadeMedidaRepository.buscarPorId(principal.empresaId, id)
        if (unidade == null) call.respond(HttpStatusCode.NotFound) else call.respond(unidade)
    }

    post("/unidades-medida") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@post
        val unidade = call.receive<UnidadeMedida>()
        call.respond(HttpStatusCode.Created, UnidadeMedidaRepository.criar(principal.empresaId, unidade))
    }

    put("/unidades-medida/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val unidade = call.receive<UnidadeMedida>()
        if (unidade.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (UnidadeMedidaRepository.atualizar(principal.empresaId, unidade)) call.respond(unidade) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/unidades-medida/{id}") {
        val principal = call.autorizarOuResponder(AppModule.CADASTROS_BASE, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (UnidadeMedidaRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
