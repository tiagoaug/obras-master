package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.domain.Equipe
import br.com.tiago.obramaster.domain.Funcionario
import br.com.tiago.obramaster.domain.Pagamento
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.StatusPagamento
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.domain.TipoContratacao
import br.com.tiago.obramaster.domain.TipoRegistroTrabalho
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
class EquipesPagamentosRoutesTest {

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
    fun equipe_criaComMembrosEAtualizaLista() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val pessoa1Id = uuid()
        val pessoa2Id = uuid()
        listOf(pessoa1Id, pessoa2Id).forEach { id ->
            client.post("/pessoas") {
                bearerAuth(token.accessToken)
                contentType(ContentType.Application.Json)
                setBody(Pessoa(id = id, nome = "Pessoa $id", tags = setOf(TagPessoa.FUNCIONARIO)))
            }
        }

        val equipe = Equipe(id = uuid(), nome = "Equipe Alvenaria", liderPessoaId = pessoa1Id, membrosIds = setOf(pessoa1Id, pessoa2Id))
        val criada = client.post("/equipes") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(equipe)
        }
        assertEquals(HttpStatusCode.Created, criada.status)

        val buscada = client.get("/equipes/${equipe.id}") { bearerAuth(token.accessToken) }.body<Equipe>()
        assertEquals(setOf(pessoa1Id, pessoa2Id), buscada.membrosIds)

        // Remove um membro via PUT — a lista de membros tem que refletir o novo conjunto, não acumular.
        val atualizada = equipe.copy(membrosIds = setOf(pessoa1Id))
        client.put("/equipes/${equipe.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(atualizada)
        }
        val depois = client.get("/equipes/${equipe.id}") { bearerAuth(token.accessToken) }.body<Equipe>()
        assertEquals(setOf(pessoa1Id), depois.membrosIds)
    }

    @Test
    fun funcionario_criarBuscarPorPessoaIdEAtualizar() = testApplication {
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
            setBody(Pessoa(id = pessoaId, nome = "Pedreiro", tags = setOf(TagPessoa.FUNCIONARIO)))
        }

        val funcionario = Funcionario(pessoaId = pessoaId, funcao = "Pedreiro", tipoContratacao = TipoContratacao.DIARIA, valorBase = 15_000L)
        val criado = client.post("/funcionarios") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(funcionario)
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val buscado = client.get("/funcionarios/$pessoaId") { bearerAuth(token.accessToken) }.body<Funcionario>()
        assertEquals("Pedreiro", buscado.funcao)

        val atualizado = funcionario.copy(valorBase = 18_000L)
        val respostaUpdate = client.put("/funcionarios/$pessoaId") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(atualizado)
        }
        assertEquals(HttpStatusCode.OK, respostaUpdate.status)
    }

    @Test
    fun registroTrabalho_criaEFiltraPorPessoaEStatusPago() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val projetoId = uuid()
        client.post("/projetos") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(br.com.tiago.obramaster.domain.Projeto(id = projetoId, nome = "Obra", orcamentoTotal = 100_000_00L, status = br.com.tiago.obramaster.domain.StatusProjeto.PLANEJAMENTO))
        }

        val pessoaId = uuid()
        val registro = RegistroTrabalho(id = uuid(), pessoaId = pessoaId, projetoId = projetoId, data = 0L, tipo = TipoRegistroTrabalho.DIARIA, valor = 15_000L, pago = false)
        client.post("/registros-trabalho") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(registro)
        }

        val naoPagos = client.get("/registros-trabalho?pago=false") { bearerAuth(token.accessToken) }.body<List<RegistroTrabalho>>()
        assertTrue(naoPagos.any { it.id == registro.id })

        val pagos = client.get("/registros-trabalho?pago=true") { bearerAuth(token.accessToken) }.body<List<RegistroTrabalho>>()
        assertTrue(pagos.none { it.id == registro.id })

        val pagamento = Pagamento(id = uuid(), pessoaId = pessoaId, periodo = "Agosto/2026", valorTotal = 15_000L, dataPagamento = 0L, status = StatusPagamento.PAGO)
        client.post("/pagamentos") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(pagamento)
        }

        val marcarComoPago = registro.copy(pago = true, pagamentoId = pagamento.id)
        client.put("/registros-trabalho/${registro.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(marcarComoPago)
        }

        val pagosDepois = client.get("/registros-trabalho?pago=true") { bearerAuth(token.accessToken) }.body<List<RegistroTrabalho>>()
        assertTrue(pagosDepois.any { it.id == registro.id && it.pagamentoId == pagamento.id })
    }
}
