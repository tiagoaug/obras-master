package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusEtapa
import br.com.tiago.obramaster.domain.StatusProjeto
import br.com.tiago.obramaster.server.db.Colaboradores
import br.com.tiago.obramaster.server.db.DatabaseFactory
import br.com.tiago.obramaster.server.db.Empresas
import br.com.tiago.obramaster.server.db.Permissoes
import br.com.tiago.obramaster.server.routes.LoginRequest
import br.com.tiago.obramaster.server.routes.TokenResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.delete
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ApplicationTest {

    private fun uuid() = Uuid.random().toString()

    private fun seedEmpresaEColaborador(
        empresaId: String,
        colaboradorId: String,
        login: String,
        senha: String,
        ehGestor: Boolean,
        permissoes: List<Pair<AppModule, NivelPermissao>> = emptyList(),
    ) {
        val hashed = PasswordHasher.hash(senha)
        transaction {
            Empresas.insert { it[id] = empresaId; it[nome] = "Empresa Teste" }
            Colaboradores.insert {
                it[id] = colaboradorId
                it[Colaboradores.empresaId] = empresaId
                it[nome] = "Colaborador Teste"
                it[Colaboradores.login] = login
                it[senhaHash] = hashed.hashBase64
                it[salt] = hashed.saltBase64
                it[ativo] = true
                it[Colaboradores.ehGestor] = ehGestor
            }
            permissoes.forEach { (modulo, nivel) ->
                Permissoes.insert {
                    it[Permissoes.colaboradorId] = colaboradorId
                    it[moduleId] = modulo.id
                    it[Permissoes.nivel] = nivel.name
                }
            }
        }
    }

    private fun setupBanco() {
        DatabaseFactory.init(url = "jdbc:h2:mem:${uuid()};DB_CLOSE_DELAY=-1")
    }

    @Test
    fun login_comCredenciaisCorretas_retornaTokens() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresaEColaborador(empresaId, uuid(), "gestor", "senha123", ehGestor = true)

        val resposta = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }

        assertEquals(HttpStatusCode.OK, resposta.status)
        val token = resposta.body<TokenResponse>()
        assertTrue(token.accessToken.isNotBlank())
        assertTrue(token.refreshToken.isNotBlank())
        assertTrue(token.ehGestor)
    }

    @Test
    fun login_comSenhaErrada_retorna401() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresaEColaborador(empresaId, uuid(), "gestor", "senha123", ehGestor = true)

        val resposta = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha-errada", empresaId))
        }

        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
    }

    @Test
    fun projetos_gestorConsegueCriarListarEExcluir() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresaEColaborador(empresaId, uuid(), "gestor", "senha123", ehGestor = true)
        val login = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val projeto = Projeto(
            id = uuid(),
            nome = "Residencial Teste",
            orcamentoTotal = 50_000_00L,
            status = StatusProjeto.PLANEJAMENTO,
        )

        val criado = client.post("/projetos") {
            bearerAuth(login.accessToken)
            contentType(ContentType.Application.Json)
            setBody(projeto)
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val listados = client.get("/projetos") { bearerAuth(login.accessToken) }
        assertEquals(HttpStatusCode.OK, listados.status)
        val lista = listados.body<List<Projeto>>()
        assertTrue(lista.any { it.id == projeto.id })

        val excluido = client.delete("/projetos/${projeto.id}") { bearerAuth(login.accessToken) }
        assertEquals(HttpStatusCode.NoContent, excluido.status)

        val listadosDepois = client.get("/projetos") { bearerAuth(login.accessToken) }.body<List<Projeto>>()
        assertTrue(listadosDepois.none { it.id == projeto.id })
    }

    @Test
    fun projetos_colaboradorSemPermissao_recebe403() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresaEColaborador(empresaId, uuid(), "sem_permissao", "senha123", ehGestor = false)
        val login = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("sem_permissao", "senha123", empresaId))
        }.body<TokenResponse>()

        val resposta = client.get("/projetos") { bearerAuth(login.accessToken) }
        assertEquals(HttpStatusCode.Forbidden, resposta.status)
    }

    @Test
    fun projetos_colaboradorComLeitura_veMasNaoCria() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresaEColaborador(
            empresaId, uuid(), "leitor", "senha123", ehGestor = false,
            permissoes = listOf(AppModule.PROJETOS to NivelPermissao.LEITURA),
        )
        val login = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("leitor", "senha123", empresaId))
        }.body<TokenResponse>()

        val listar = client.get("/projetos") { bearerAuth(login.accessToken) }
        assertEquals(HttpStatusCode.OK, listar.status)

        val criar = client.post("/projetos") {
            bearerAuth(login.accessToken)
            contentType(ContentType.Application.Json)
            setBody(Projeto(id = uuid(), nome = "X", orcamentoTotal = 1000L, status = StatusProjeto.PLANEJAMENTO))
        }
        assertEquals(HttpStatusCode.Forbidden, criar.status)
    }

    @Test
    fun etapas_criarEListarPorProjeto() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedEmpresaEColaborador(empresaId, uuid(), "gestor", "senha123", ehGestor = true)
        val login = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val projeto = Projeto(id = uuid(), nome = "Obra", orcamentoTotal = 100_000_00L, status = StatusProjeto.PLANEJAMENTO)
        client.post("/projetos") {
            bearerAuth(login.accessToken)
            contentType(ContentType.Application.Json)
            setBody(projeto)
        }

        val etapa = Etapa(id = uuid(), projetoId = projeto.id, nome = "Fundação", ordem = 1, orcamentoEtapa = 10_000_00L, status = StatusEtapa.NAO_INICIADA)
        val criada = client.post("/projetos/${projeto.id}/etapas") {
            bearerAuth(login.accessToken)
            contentType(ContentType.Application.Json)
            setBody(etapa)
        }
        assertEquals(HttpStatusCode.Created, criada.status)

        val listadas = client.get("/projetos/${projeto.id}/etapas") { bearerAuth(login.accessToken) }
            .body<List<Etapa>>()
        assertEquals(1, listadas.size)
        assertEquals("Fundação", listadas.single().nome)
    }

    @Test
    fun rotaProtegida_semToken_retorna401() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val resposta = client.get("/projetos")
        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
    }
}
