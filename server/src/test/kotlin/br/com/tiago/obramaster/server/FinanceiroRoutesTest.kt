package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import br.com.tiago.obramaster.domain.TipoConta
import br.com.tiago.obramaster.domain.TipoLancamento
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
class FinanceiroRoutesTest {

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
    fun contas_ciclCompletoCrud() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val conta = Conta(id = uuid(), nome = "Caixa da Obra", tipo = TipoConta.CAIXA, saldoInicial = 100_000L, dataSaldoInicial = 0L)
        val criada = client.post("/contas") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(conta)
        }
        assertEquals(HttpStatusCode.Created, criada.status)

        val atualizada = conta.copy(nome = "Caixa Principal")
        val respostaUpdate = client.put("/contas/${conta.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(atualizada)
        }
        assertEquals(HttpStatusCode.OK, respostaUpdate.status)

        val lista = client.get("/contas") { bearerAuth(token.accessToken) }.body<List<Conta>>()
        assertEquals("Caixa Principal", lista.single { it.id == conta.id }.nome)

        val excluida = client.delete("/contas/${conta.id}") { bearerAuth(token.accessToken) }
        assertEquals(HttpStatusCode.NoContent, excluida.status)
        val listaDepois = client.get("/contas") { bearerAuth(token.accessToken) }.body<List<Conta>>()
        assertTrue(listaDepois.none { it.id == conta.id })
    }

    @Test
    fun categoriaFinanceira_padraoDoSistema_naoPodeSerExcluida() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val categoria = CategoriaFinanceira(
            id = uuid(), nome = "Materiais", tipo = TipoLancamento.DESPESA,
            naturezaPadrao = NaturezaLancamento.CONTABIL, cor = "#FF0000", padraoDoSistema = true,
        )
        client.post("/categorias-financeiras") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(categoria)
        }

        val exclusao = client.delete("/categorias-financeiras/${categoria.id}") { bearerAuth(token.accessToken) }
        assertEquals(HttpStatusCode.Conflict, exclusao.status)

        val naoPadrao = categoria.copy(id = uuid(), padraoDoSistema = false)
        client.post("/categorias-financeiras") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(naoPadrao)
        }
        val exclusaoOk = client.delete("/categorias-financeiras/${naoPadrao.id}") { bearerAuth(token.accessToken) }
        assertEquals(HttpStatusCode.NoContent, exclusaoOk.status)
    }

    @Test
    fun centroDeCusto_criarEListar() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val centro = CentroDeCusto(id = uuid(), nome = "Administrativo", tipo = TipoCentroDeCusto.ADMINISTRATIVO)
        val criado = client.post("/centros-de-custo") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(centro)
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val lista = client.get("/centros-de-custo") { bearerAuth(token.accessToken) }.body<List<CentroDeCusto>>()
        assertTrue(lista.any { it.id == centro.id })
    }

    @Test
    fun lancamentoFinanceiro_criaEListaMasNaoTemRotaDeEdicaoOuExclusao() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val categoria = CategoriaFinanceira(id = uuid(), nome = "Materiais", tipo = TipoLancamento.DESPESA, naturezaPadrao = NaturezaLancamento.CONTABIL, cor = "#FF0000")
        client.post("/categorias-financeiras") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(categoria)
        }
        val centro = CentroDeCusto(id = uuid(), nome = "Administrativo", tipo = TipoCentroDeCusto.ADMINISTRATIVO)
        client.post("/centros-de-custo") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(centro)
        }

        val lancamento = LancamentoFinanceiro(
            id = uuid(), tipo = TipoLancamento.DESPESA, categoriaId = categoria.id, centroDeCustoId = centro.id,
            natureza = NaturezaLancamento.CONTABIL, descricao = "Compra de cimento", valor = 15_000L, data = 0L,
            formaPagamento = "PIX",
        )
        val criado = client.post("/lancamentos-financeiros") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(lancamento)
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val lista = client.get("/lancamentos-financeiros") { bearerAuth(token.accessToken) }.body<List<LancamentoFinanceiro>>()
        assertTrue(lista.any { it.id == lancamento.id })

        // Sem rota PUT/DELETE de propósito (imutabilidade) — bate numa rota que não existe, 404 de roteamento.
        val tentativaExclusao = client.delete("/lancamentos-financeiros/${lancamento.id}") { bearerAuth(token.accessToken) }
        assertEquals(HttpStatusCode.NotFound, tentativaExclusao.status)
    }
}
