package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.LancamentoFinanceiroRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/** Sem PUT/DELETE de propósito — ver nota em LancamentoFinanceiroRepository sobre imutabilidade. */
fun Route.lancamentoFinanceiroRoutes() {
    get("/lancamentos-financeiros") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        val projetoId = call.request.queryParameters["projetoId"]
        call.respond(LancamentoFinanceiroRepository.listar(principal.empresaId, projetoId))
    }

    get("/lancamentos-financeiros/{id}") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val lancamento = LancamentoFinanceiroRepository.buscarPorId(principal.empresaId, id)
        if (lancamento == null) call.respond(HttpStatusCode.NotFound) else call.respond(lancamento)
    }

    post("/lancamentos-financeiros") {
        val principal = call.autorizarOuResponder(AppModule.FINANCEIRO, exigirEscrita = true) ?: return@post
        val lancamento = call.receive<LancamentoFinanceiro>()
        call.respond(HttpStatusCode.Created, LancamentoFinanceiroRepository.criar(principal.empresaId, lancamento))
    }
}
