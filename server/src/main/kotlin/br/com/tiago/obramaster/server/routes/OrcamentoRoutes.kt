package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.OrcamentoRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable

@Serializable
data class OrcamentoComItens(val orcamento: Orcamento, val itens: List<ItemOrcamento>)

fun Route.orcamentoRoutes() {
    get("/orcamentos") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = false) ?: return@get
        val resultado = OrcamentoRepository.listar(principal.empresaId).map { (orcamento, itens) -> OrcamentoComItens(orcamento, itens) }
        call.respond(resultado)
    }

    get("/orcamentos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val resultado = OrcamentoRepository.buscarPorId(principal.empresaId, id)
        if (resultado == null) call.respond(HttpStatusCode.NotFound) else call.respond(OrcamentoComItens(resultado.first, resultado.second))
    }

    post("/orcamentos") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = true) ?: return@post
        val corpo = call.receive<OrcamentoComItens>()
        val (orcamento, itens) = OrcamentoRepository.criar(principal.empresaId, corpo.orcamento, corpo.itens)
        call.respond(HttpStatusCode.Created, OrcamentoComItens(orcamento, itens))
    }

    put("/orcamentos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val corpo = call.receive<OrcamentoComItens>()
        if (corpo.orcamento.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (OrcamentoRepository.atualizar(principal.empresaId, corpo.orcamento, corpo.itens)) call.respond(corpo) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/orcamentos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.ORCAMENTOS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (OrcamentoRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
