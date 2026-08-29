package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Pagamento
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.PagamentoRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.pagamentoRoutes() {
    get("/pagamentos") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        val pessoaId = call.request.queryParameters["pessoaId"]
        call.respond(PagamentoRepository.listar(principal.empresaId, pessoaId))
    }

    get("/pagamentos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val pagamento = PagamentoRepository.buscarPorId(principal.empresaId, id)
        if (pagamento == null) call.respond(HttpStatusCode.NotFound) else call.respond(pagamento)
    }

    post("/pagamentos") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@post
        val pagamento = call.receive<Pagamento>()
        call.respond(HttpStatusCode.Created, PagamentoRepository.criar(principal.empresaId, pagamento))
    }

    put("/pagamentos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val pagamento = call.receive<Pagamento>()
        if (pagamento.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (PagamentoRepository.atualizar(principal.empresaId, pagamento)) call.respond(pagamento) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/pagamentos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (PagamentoRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
