package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.server.db.Colaboradores
import br.com.tiago.obramaster.server.db.DatabaseFactory
import br.com.tiago.obramaster.server.db.Empresas
import br.com.tiago.obramaster.server.routes.LoginRequest
import br.com.tiago.obramaster.server.routes.TokenResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
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
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class PessoaRoutesTest {

    private fun uuid() = Uuid.random().toString()

    private fun setupBanco() {
        DatabaseFactory.init(url = "jdbc:h2:mem:${uuid()};DB_CLOSE_DELAY=-1")
    }

    private fun seedGestor(empresaId: String, login: String = "gestor", senha: String = "senha123") {
        val hashed = PasswordHasher.hash(senha)
        transaction {
            Empresas.insert { it[id] = empresaId; it[nome] = "Empresa Teste" }
            Colaboradores.insert {
                it[id] = uuid()
                it[Colaboradores.empresaId] = empresaId
                it[nome] = "Gestor Teste"
                it[Colaboradores.login] = login
                it[senhaHash] = hashed.hashBase64
                it[salt] = hashed.saltBase64
                it[ativo] = true
                it[ehGestor] = true
            }
        }
    }

    @Test
    fun pessoas_cicloCompletoCrudComTagsMultiplas() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val pessoa = Pessoa(id = uuid(), nome = "João Pedreiro", tags = setOf(TagPessoa.FUNCIONARIO, TagPessoa.FORNECEDOR))
        val criada = client.post("/pessoas") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(pessoa)
        }
        assertEquals(HttpStatusCode.Created, criada.status)

        val buscada = client.get("/pessoas/${pessoa.id}") { bearerAuth(token.accessToken) }.body<Pessoa>()
        assertEquals(setOf(TagPessoa.FUNCIONARIO, TagPessoa.FORNECEDOR), buscada.tags)

        val atualizada = pessoa.copy(nome = "João Silva", tags = setOf(TagPessoa.CLIENTE))
        val respostaUpdate = client.put("/pessoas/${pessoa.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(atualizada)
        }
        assertEquals(HttpStatusCode.OK, respostaUpdate.status)
        val depoisDoUpdate = client.get("/pessoas/${pessoa.id}") { bearerAuth(token.accessToken) }.body<Pessoa>()
        assertEquals("João Silva", depoisDoUpdate.nome)
        assertEquals(setOf(TagPessoa.CLIENTE), depoisDoUpdate.tags)

        val excluida = client.delete("/pessoas/${pessoa.id}") { bearerAuth(token.accessToken) }
        assertEquals(HttpStatusCode.NoContent, excluida.status)
        val lista = client.get("/pessoas") { bearerAuth(token.accessToken) }.body<List<Pessoa>>()
        assertTrue(lista.none { it.id == pessoa.id })
    }

    @Test
    fun pessoas_isoladasPorEmpresa() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaA = uuid()
        val empresaB = uuid()
        seedGestor(empresaA, login = "gestorA")
        seedGestor(empresaB, login = "gestorB")

        val tokenA = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestorA", "senha123", empresaA))
        }.body<TokenResponse>()
        val tokenB = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestorB", "senha123", empresaB))
        }.body<TokenResponse>()

        val pessoaA = Pessoa(id = uuid(), nome = "Pessoa da Empresa A", tags = setOf(TagPessoa.CLIENTE))
        client.post("/pessoas") {
            bearerAuth(tokenA.accessToken)
            contentType(ContentType.Application.Json)
            setBody(pessoaA)
        }

        val listaB = client.get("/pessoas") { bearerAuth(tokenB.accessToken) }.body<List<Pessoa>>()
        assertTrue(listaB.none { it.id == pessoaA.id })

        val buscaCruzada = client.get("/pessoas/${pessoaA.id}") { bearerAuth(tokenB.accessToken) }
        assertEquals(HttpStatusCode.NotFound, buscaCruzada.status)
    }
}
