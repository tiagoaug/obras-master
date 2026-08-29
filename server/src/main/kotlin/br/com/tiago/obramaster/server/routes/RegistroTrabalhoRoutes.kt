package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.server.auth.autorizarOuResponder
import br.com.tiago.obramaster.server.repository.RegistroTrabalhoRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.registroTrabalhoRoutes() {
    get("/registros-trabalho") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        val pessoaId = call.request.queryParameters["pessoaId"]
        val pago = call.request.queryParameters["pago"]?.toBooleanStrictOrNull()
        call.respond(RegistroTrabalhoRepository.listar(principal.empresaId, pessoaId, pago))
    }

    get("/registros-trabalho/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = false) ?: return@get
        val id = call.parameters["id"]!!
        val registro = RegistroTrabalhoRepository.buscarPorId(principal.empresaId, id)
        if (registro == null) call.respond(HttpStatusCode.NotFound) else call.respond(registro)
    }

    post("/registros-trabalho") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@post
        val registro = call.receive<RegistroTrabalho>()
        call.respond(HttpStatusCode.Created, RegistroTrabalhoRepository.criar(principal.empresaId, registro))
    }

    put("/registros-trabalho/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@put
        val id = call.parameters["id"]!!
        val registro = call.receive<RegistroTrabalho>()
        if (registro.id != id) {
            call.respond(HttpStatusCode.BadRequest, mapOf("erro" to "id do corpo não bate com a URL"))
            return@put
        }
        if (RegistroTrabalhoRepository.atualizar(principal.empresaId, registro)) call.respond(registro) else call.respond(HttpStatusCode.NotFound)
    }

    delete("/registros-trabalho/{id}") {
        val principal = call.autorizarOuResponder(AppModule.EQUIPES, exigirEscrita = true) ?: return@delete
        val id = call.parameters["id"]!!
        if (RegistroTrabalhoRepository.excluir(principal.empresaId, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
    }
}
