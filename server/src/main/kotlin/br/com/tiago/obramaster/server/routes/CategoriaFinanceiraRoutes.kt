package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.CategoriaFinanceiraRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.categoriaFinanceiraRoutes() {
    get("/categorias-financeiras") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        call.respond(CategoriaFinanceiraRepository.listar(principal.empresaId))
    }

    get("/categorias-financeiras/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val categoria = CategoriaFinanceiraRepository.buscarPorId(principal.empresaId, id)
        if (categoria == null) call.respond(HttpStatusCode.NotFound) else call.respond(categoria)
    }

    post("/categorias-financeiras") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@post
        val categoria = call.receive<CategoriaFinanceira>()
        call.respond(HttpStatusCode.Created, CategoriaFinanceiraRepository.criar(principal.empresaId, categoria))
    }

    put("/categorias-financeiras/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val categoria = call.receive<CategoriaFinanceira>()
        if (categoria.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (CategoriaFinanceiraRepository.atualizar(principal.empresaId, categoria)) call.respond(categoria) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/categorias-financeiras/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        when (CategoriaFinanceiraRepository.excluir(principal.empresaId, id)) {
            CategoriaFinanceiraRepository.ResultadoExclusao.EXCLUIDA -> call.respond(HttpStatusCode.NoContent)
            CategoriaFinanceiraRepository.ResultadoExclusao.NAO_ENCONTRADA -> call.respond(HttpStatusCode.NotFound)
            CategoriaFinanceiraRepository.ResultadoExclusao.PADRAO_DO_SISTEMA ->
                call.respond(HttpStatusCode.Conflict, mapOf("erro" to "Categoria padrão do sistema não pode ser excluída, só inativada (PUT com ativo=false)"))
        }
    }
}
