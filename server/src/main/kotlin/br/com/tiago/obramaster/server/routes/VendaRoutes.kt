package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.Venda
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.VendaRepository
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
data class VendaComParcelas(val venda: Venda, val parcelas: List<ParcelaVenda>)

fun Route.vendaRoutes() {
    get("/vendas") {
        val principal = call.autorizarOuResponder(AppModule.VENDAS, exigirEscrita = false) ?: return@get
        val resultado = VendaRepository.listar(principal.empresaId).map { (venda, parcelas) -> VendaComParcelas(venda, parcelas) }
        call.respond(resultado)
    }

    get("/vendas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.VENDAS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val resultado = VendaRepository.buscarPorId(principal.empresaId, id)
        if (resultado == null) call.respond(HttpStatusCode.NotFound) else call.respond(VendaComParcelas(resultado.first, resultado.second))
    }

    post("/vendas") {
        val principal = call.autorizarOuResponder(AppModule.VENDAS, exigirEscrita = true) ?: return@post
        val corpo = call.receive<VendaComParcelas>()
        val (venda, parcelas) = VendaRepository.criar(principal.empresaId, corpo.venda, corpo.parcelas)
        call.respond(HttpStatusCode.Created, VendaComParcelas(venda, parcelas))
    }

    put("/vendas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.VENDAS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val corpo = call.receive<VendaComParcelas>()
        if (corpo.venda.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (VendaRepository.atualizar(principal.empresaId, corpo.venda, corpo.parcelas)) call.respond(corpo) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/vendas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.VENDAS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (VendaRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
