package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.ContaRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.contaRoutes() {
    get("/contas") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        call.respond(ContaRepository.listar(principal.empresaId))
    }

    get("/contas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val conta = ContaRepository.buscarPorId(principal.empresaId, id)
        if (conta == null) call.respond(HttpStatusCode.NotFound) else call.respond(conta)
    }

    post("/contas") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@post
        val conta = call.receive<Conta>()
        call.respond(HttpStatusCode.Created, ContaRepository.criar(principal.empresaId, conta))
    }

    put("/contas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val conta = call.receive<Conta>()
        if (conta.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (ContaRepository.atualizar(principal.empresaId, conta)) call.respond(conta) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/contas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (ContaRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
