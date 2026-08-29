package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.PedidoCompraRepository
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
data class PedidoCompraComItens(val pedido: PedidoCompra, val itens: List<ItemCompra>)

fun Route.pedidoCompraRoutes() {
    get("/pedidos-compra") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = false) ?: return@get
        val resultado = PedidoCompraRepository.listar(principal.empresaId).map { (pedido, itens) -> PedidoCompraComItens(pedido, itens) }
        call.respond(resultado)
    }

    get("/pedidos-compra/{id}") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val resultado = PedidoCompraRepository.buscarPorId(principal.empresaId, id)
        if (resultado == null) call.respond(HttpStatusCode.NotFound) else call.respond(PedidoCompraComItens(resultado.first, resultado.second))
    }

    post("/pedidos-compra") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = true) ?: return@post
        val corpo = call.receive<PedidoCompraComItens>()
        val (pedido, itens) = PedidoCompraRepository.criar(principal.empresaId, corpo.pedido, corpo.itens)
        call.respond(HttpStatusCode.Created, PedidoCompraComItens(pedido, itens))
    }

    put("/pedidos-compra/{id}") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val corpo = call.receive<PedidoCompraComItens>()
        if (corpo.pedido.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (PedidoCompraRepository.atualizar(principal.empresaId, corpo.pedido, corpo.itens)) call.respond(corpo) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/pedidos-compra/{id}") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (PedidoCompraRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
