package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.ProjetoRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

/** SPEC_OBRA_MASTER_KMP.md §6.2 — CRUD /projetos, sempre escopado por empresaId (do token) e
 * validado por PermissionEngine (módulo PROJETOS), igual ao cliente faz localmente. */
fun Route.projetoRoutes() {
    get("/projetos") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = false) ?: return@get
        call.respond(ProjetoRepository.listar(principal.empresaId))
    }

    get("/projetos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val projeto = ProjetoRepository.buscarPorId(principal.empresaId, id)
        if (projeto == null) call.respond(HttpStatusCode.NotFound) else call.respond(projeto)
    }

    post("/projetos") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = true) ?: return@post
        val projeto = call.receive<Projeto>()
        call.respond(HttpStatusCode.Created, ProjetoRepository.criar(principal.empresaId, projeto))
    }

    put("/projetos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val projeto = call.receive<Projeto>()
        if (projeto.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        val atualizado = ProjetoRepository.atualizar(principal.empresaId, projeto)
        if (atualizado) call.respond(projeto) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/projetos/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        val excluido = ProjetoRepository.excluir(principal.empresaId, id)
        if (excluido) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
