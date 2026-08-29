package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.PessoaRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.pessoaRoutes() {
    get("/pessoas") {
        val principal = call.autorizarOuResponder(AppModule.PESSOAS, exigirEscrita = false) ?: return@get
        call.respond(PessoaRepository.listar(principal.empresaId))
    }

    get("/pessoas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PESSOAS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val pessoa = PessoaRepository.buscarPorId(principal.empresaId, id)
        if (pessoa == null) call.respond(HttpStatusCode.NotFound) else call.respond(pessoa)
    }

    post("/pessoas") {
        val principal = call.autorizarOuResponder(AppModule.PESSOAS, exigirEscrita = true) ?: return@post
        val pessoa = call.receive<Pessoa>()
        call.respond(HttpStatusCode.Created, PessoaRepository.criar(principal.empresaId, pessoa))
    }

    put("/pessoas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PESSOAS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val pessoa = call.receive<Pessoa>()
        if (pessoa.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (PessoaRepository.atualizar(principal.empresaId, pessoa)) call.respond(pessoa) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/pessoas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PESSOAS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (PessoaRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
