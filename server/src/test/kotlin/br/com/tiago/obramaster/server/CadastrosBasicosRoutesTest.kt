package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.domain.Cor
import br.com.tiago.obramaster.domain.Fornecedor
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.domain.UnidadeMedida
import br.com.tiago.obramaster.server.db.Colaboradores
import br.com.tiago.obramaster.server.db.DatabaseFactory
import br.com.tiago.obramaster.server.db.Empresas
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
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CadastrosBasicosRoutesTest {

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
    fun corUnidadeEMaterial_criamListamEAtualizam() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val cor = Cor(id = uuid(), nome = "Branco Neve", hex = "#FFFFFF")
        client.post("/cores") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(cor)
        }

        val unidade = UnidadeMedida(id = uuid(), sigla = "sc", nome = "Saco")
        client.post("/unidades-medida") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(unidade)
        }

        val material = Material(id = uuid(), nome = "Cimento CP-II", unidadePadrao = unidade.sigla, precoReferencia = 3_200L, corId = cor.id)
        val criado = client.post("/materiais") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(material)
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val atualizado = material.copy(precoReferencia = 3_500L)
        val respostaUpdate = client.put("/materiais/${material.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(atualizado)
        }
        assertEquals(HttpStatusCode.OK, respostaUpdate.status)

        val lista = client.get("/materiais") { bearerAuth(token.accessToken) }.body<List<Material>>()
        assertEquals(3_500L, lista.single { it.id == material.id }.precoReferencia)
        assertEquals(cor.id, lista.single { it.id == material.id }.corId)
    }

    @Test
    fun fornecedor_extensaoDePessoaGatedPeloModuloCompras() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val pessoaId = uuid()
        client.post("/pessoas") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(Pessoa(id = pessoaId, nome = "Depósito Central", tags = setOf(TagPessoa.FORNECEDOR)))
        }

        val fornecedor = Fornecedor(pessoaId = pessoaId, cnpjCpf = "00.000.000/0001-00")
        val criado = client.post("/fornecedores") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(fornecedor)
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val lista = client.get("/fornecedores") { bearerAuth(token.accessToken) }.body<List<Fornecedor>>()
        assertTrue(lista.any { it.pessoaId == pessoaId })
    }
}
