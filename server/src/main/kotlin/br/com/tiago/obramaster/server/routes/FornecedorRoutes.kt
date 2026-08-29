package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Fornecedor
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.FornecedorRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.fornecedorRoutes() {
    get("/fornecedores") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = false) ?: return@get
        call.respond(FornecedorRepository.listar(principal.empresaId))
    }

    get("/fornecedores/{pessoaId}") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = false) ?: return@get
        val pessoaId = call.parameters["pessoaId"]!!
        val fornecedor = FornecedorRepository.buscarPorPessoaId(principal.empresaId, pessoaId)
        if (fornecedor == null) call.respond(HttpStatusCode.NotFound) else call.respond(fornecedor)
    }

    post("/fornecedores") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = true) ?: return@post
        val fornecedor = call.receive<Fornecedor>()
        call.respond(HttpStatusCode.Created, FornecedorRepository.criar(principal.empresaId, fornecedor))
    }

    put("/fornecedores/{pessoaId}") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = true) ?: return@put
        val pessoaId = call.parameters["pessoaId"]!!
        val fornecedor = call.receive<Fornecedor>()
        if (fornecedor.pessoaId != pessoaId) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "pessoaId do corpo não bate com a URL"))
            return@put
        }
        if (FornecedorRepository.atualizar(principal.empresaId, fornecedor)) call.respond(fornecedor) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/fornecedores/{pessoaId}") {
        val principal = call.autorizarOuResponder(AppModule.COMPRAS, exigirEscrita = true) ?: return@delete
        val pessoaId = call.parameters["pessoaId"]!!
        if (FornecedorRepository.excluir(principal.empresaId, pessoaId)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
