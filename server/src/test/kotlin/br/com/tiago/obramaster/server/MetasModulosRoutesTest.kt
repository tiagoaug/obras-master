package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.EscopoMeta
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.TipoMeta
import br.com.tiago.obramaster.server.db.Colaboradores
import br.com.tiago.obramaster.server.db.DatabaseFactory
import br.com.tiago.obramaster.server.db.Empresas
import br.com.tiago.obramaster.server.routes.DefinirModuloRequest
import br.com.tiago.obramaster.server.routes.LoginRequest
import br.com.tiago.obramaster.server.routes.TokenResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MetasModulosRoutesTest {

    private fun uuid() = Uuid.random().toString()

    private fun setupBanco() {
        DatabaseFactory.init(url = "jdbc:h2:mem:${uuid()};DB_CLOSE_DELAY=-1")
    }

    private fun seedColaborador(empresaId: String, login: String, senha: String = "senha123", ehGestor: Boolean) {
        val hashed = PasswordHasher.hash(senha)
        transaction {
            Colaboradores.insert {
                it[id] = uuid()
                it[Colaboradores.empresaId] = empresaId
                it[nome] = "Colaborador $login"
                it[Colaboradores.login] = login
                it[senhaHash] = hashed.hashBase64
                it[salt] = hashed.saltBase64
                it[ativo] = true
                it[Colaboradores.ehGestor] = ehGestor
            }
        }
    }

    private fun seedEmpresa(empresaId: String) = transaction { Empresas.insert { it[id] = empresaId; it[nome] = "Empresa Teste" } }

    @Test
    fun meta_criaListaEMarcaComoConcluida() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresa(empresaId)
        seedColaborador(empresaId, "gestor", ehGestor = true)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val meta = Meta(id = uuid(), escopo = EscopoMeta.GERAL, titulo = "Faturar R$500k no ano", tipo = TipoMeta.FINANCEIRA, valorAlvo = 500_000_00L)
        val criada = client.post("/metas") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(meta)
        }
        assertEquals(HttpStatusCode.Created, criada.status)

        val concluida = meta.copy(concluida = true)
        client.put("/metas/${meta.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(concluida)
        }

        val lista = client.get("/metas") { bearerAuth(token.accessToken) }.body<List<Meta>>()
        assertTrue(lista.single { it.id == meta.id }.concluida)
    }

    @Test
    fun modulos_todosHabilitadosPorPadraoEGestorPodeDesativar() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresa(empresaId)
        seedColaborador(empresaId, "gestor", ehGestor = true)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val estadoInicial = client.get("/modulos") { bearerAuth(token.accessToken) }.body<Map<String, Boolean>>()
        assertTrue(estadoInicial.values.all { it })
        assertEquals(AppModule.entries.size, estadoInicial.size)

        val respostaDesativar = client.put("/modulos/${AppModule.VENDAS.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(DefinirModuloRequest(enabled = false))
        }
        assertEquals(HttpStatusCode.OK, respostaDesativar.status)

        val estadoDepois = client.get("/modulos") { bearerAuth(token.accessToken) }.body<Map<String, Boolean>>()
        assertFalse(estadoDepois.getValue(AppModule.VENDAS.id))
        assertTrue(estadoDepois.getValue(AppModule.PROJETOS.id))
    }

    @Test
    fun modulos_colaboradorNaoGestorNaoPodeAlterar() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresa(empresaId)
        seedColaborador(empresaId, "colaborador", ehGestor = false)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("colaborador", "senha123", empresaId))
        }.body<TokenResponse>()

        // Leitura é liberada mesmo sem ser Gestor.
        val leitura = client.get("/modulos") { bearerAuth(token.accessToken) }
        assertEquals(HttpStatusCode.OK, leitura.status)

        val tentativa = client.put("/modulos/${AppModule.VENDAS.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(DefinirModuloRequest(enabled = false))
        }
        assertEquals(HttpStatusCode.Forbidden, tentativa.status)
    }
}
