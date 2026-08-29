package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.CentroDeCustoRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.centroDeCustoRoutes() {
    get("/centros-de-custo") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        call.respond(CentroDeCustoRepository.listar(principal.empresaId))
    }

    get("/centros-de-custo/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val centro = CentroDeCustoRepository.buscarPorId(principal.empresaId, id)
        if (centro == null) call.respond(HttpStatusCode.NotFound) else call.respond(centro)
    }

    post("/centros-de-custo") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@post
        val centro = call.receive<CentroDeCusto>()
        call.respond(HttpStatusCode.Created, CentroDeCustoRepository.criar(principal.empresaId, centro))
    }

    put("/centros-de-custo/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val centro = call.receive<CentroDeCusto>()
        if (centro.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (CentroDeCustoRepository.atualizar(principal.empresaId, centro)) call.respond(centro) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/centros-de-custo/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (CentroDeCustoRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
