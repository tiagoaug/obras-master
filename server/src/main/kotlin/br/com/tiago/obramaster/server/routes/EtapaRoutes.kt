package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.EtapaRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

/** Etapas pertencem a um Projeto (mesmo módulo de permissão: PROJETOS, SPEC_OBRA_MASTER.md §4.1). */
fun Route.etapaRoutes() {
    get("/projetos/{projetoId}/etapas") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = false) ?: return@get
        val projetoId = call.parameters["projetoId"]!!
        call.respond(EtapaRepository.listarPorProjeto(principal.empresaId, projetoId))
    }

    get("/etapas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val etapa = EtapaRepository.buscarPorId(principal.empresaId, id)
        if (etapa == null) call.respond(HttpStatusCode.NotFound) else call.respond(etapa)
    }

    post("/projetos/{projetoId}/etapas") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = true) ?: return@post
        val projetoId = call.parameters["projetoId"]!!
        val etapa = call.receive<Etapa>()
        if (etapa.projetoId != projetoId) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "projetoId do corpo não bate com a URL"))
            return@post
        }
        call.respond(HttpStatusCode.Created, EtapaRepository.criar(principal.empresaId, etapa))
    }

    put("/etapas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val etapa = call.receive<Etapa>()
        if (etapa.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        val atualizado = EtapaRepository.atualizar(principal.empresaId, etapa)
        if (atualizado) call.respond(etapa) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/etapas/{id}") {
        val principal = call.autorizarOuResponder(AppModule.PROJETOS, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        val excluido = EtapaRepository.excluir(principal.empresaId, id)
        if (excluido) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
