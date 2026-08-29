package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.server.auth.JwtService
import br.com.tiago.obramaster.server.db.DatabaseFactory
import br.com.tiago.obramaster.server.routes.authRoutes
import br.com.tiago.obramaster.server.routes.categoriaFinanceiraRoutes
import br.com.tiago.obramaster.server.routes.centroDeCustoRoutes
import br.com.tiago.obramaster.server.routes.configBdiRoutes
import br.com.tiago.obramaster.server.routes.contaRoutes
import br.com.tiago.obramaster.server.routes.corRoutes
import br.com.tiago.obramaster.server.routes.equipeRoutes
import br.com.tiago.obramaster.server.routes.etapaRoutes
import br.com.tiago.obramaster.server.routes.fornecedorRoutes
import br.com.tiago.obramaster.server.routes.funcionarioRoutes
import br.com.tiago.obramaster.server.routes.lancamentoFinanceiroRoutes
import br.com.tiago.obramaster.server.routes.materialRoutes
import br.com.tiago.obramaster.server.routes.meRoutes
import br.com.tiago.obramaster.server.routes.metaRoutes
import br.com.tiago.obramaster.server.routes.moduloRoutes
import br.com.tiago.obramaster.server.routes.orcamentoRoutes
import br.com.tiago.obramaster.server.routes.pagamentoRoutes
import br.com.tiago.obramaster.server.routes.pedidoCompraRoutes
import br.com.tiago.obramaster.server.routes.pessoaRoutes
import br.com.tiago.obramaster.server.routes.projetoRoutes
import br.com.tiago.obramaster.server.routes.registroTrabalhoRoutes
import br.com.tiago.obramaster.server.routes.unidadeMedidaRoutes
import br.com.tiago.obramaster.server.routes.vendaRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = Env.port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("erro" to (cause.message ?: "Erro interno")))
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim("tipo").asString() == "ACCESS") JWTPrincipal(credential.payload) else null
            }
        }
        jwt("auth-jwt-refresh") {
            verifier(JwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim("tipo").asString() == "REFRESH") JWTPrincipal(credential.payload) else null
            }
        }
    }

    routing {
        authRoutes()
        authenticate("auth-jwt") {
            meRoutes()
            projetoRoutes()
            etapaRoutes()
            contaRoutes()
            categoriaFinanceiraRoutes()
            centroDeCustoRoutes()
            lancamentoFinanceiroRoutes()
            pessoaRoutes()
            equipeRoutes()
            funcionarioRoutes()
            pagamentoRoutes()
            registroTrabalhoRoutes()
            corRoutes()
            unidadeMedidaRoutes()
            materialRoutes()
            fornecedorRoutes()
            pedidoCompraRoutes()
            configBdiRoutes()
            orcamentoRoutes()
            vendaRoutes()
            metaRoutes()
            moduloRoutes()
        }
    }
}
