package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Funcionario
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.FuncionarioRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.funcionarioRoutes() {
    get("/funcionarios") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        call.respond(FuncionarioRepository.listar(principal.empresaId))
    }

    get("/funcionarios/{pessoaId}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        val pessoaId = call.parameters["pessoaId"]!!
        val funcionario = FuncionarioRepository.buscarPorPessoaId(principal.empresaId, pessoaId)
        if (funcionario == null) call.respond(HttpStatusCode.NotFound) else call.respond(funcionario)
    }

    post("/funcionarios") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@post
        val funcionario = call.receive<Funcionario>()
        call.respond(HttpStatusCode.Created, FuncionarioRepository.criar(principal.empresaId, funcionario))
    }

    put("/funcionarios/{pessoaId}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@put
        val pessoaId = call.parameters["pessoaId"]!!
        val funcionario = call.receive<Funcionario>()
        if (funcionario.pessoaId != pessoaId) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "pessoaId do corpo não bate com a URL"))
            return@put
        }
        if (FuncionarioRepository.atualizar(principal.empresaId, funcionario)) call.respond(funcionario) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/funcionarios/{pessoaId}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@delete
        val pessoaId = call.parameters["pessoaId"]!!
        if (FuncionarioRepository.excluir(principal.empresaId, pessoaId)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
