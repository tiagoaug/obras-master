package br.com.tiago.obramaster.server.routes

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.server.auth.requestPrincipal
import br.com.tiago.obramaster.server.repository.ModuloRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable

@Serializable
data class DefinirModuloRequest(val enabled: Boolean)

/** ModuleRegistry.kt (client) — "Só o Gestor pode alterar" — checado direto aqui (não é uma
 * permissão por AppModule, é uma checagem de papel). Leitura é liberada pra qualquer colaborador
 * autenticado, igual ao StateFlow do client ser observado por todas as telas. */
fun Route.moduloRoutes() {
    get("/modulos") {
        val principal = call.requestPrincipal()
        call.respond(ModuloRepository.listar(principal.empresaId))
    }

    put("/modulos/{moduleId}") {
        val principal = call.requestPrincipal()
        if (!principal.ehGestor) {
            call.respond(HttpStatusCode.Forbidden, mapOf("erro" to "Só o Gestor pode ativar/desativar módulos"))
            return@put
        }
        val moduleId = call.parameters["moduleId"]!!
        if (AppModule.fromId(moduleId) == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("erro" to "Módulo desconhecido: $moduleId"))
            return@put
        }
        val corpo = call.receive<DefinirModuloRequest>()
        ModuloRepository.definir(principal.empresaId, moduleId, corpo.enabled)
        call.respond(ModuloRepository.listar(principal.empresaId))
    }
}
