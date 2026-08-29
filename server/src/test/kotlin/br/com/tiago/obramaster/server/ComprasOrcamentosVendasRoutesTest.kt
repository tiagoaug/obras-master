package br.com.tiago.obramaster.server

import br.com.tiago.obramaster.core.auth.PasswordHasher
import br.com.tiago.obramaster.domain.ConfigBDI
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusOrcamento
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import br.com.tiago.obramaster.domain.StatusProjeto
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.TipoItemOrcamento
import br.com.tiago.obramaster.domain.Venda
import br.com.tiago.obramaster.server.db.Colaboradores
import br.com.tiago.obramaster.server.db.DatabaseFactory
import br.com.tiago.obramaster.server.db.Empresas
import br.com.tiago.obramaster.server.routes.LoginRequest
import br.com.tiago.obramaster.server.routes.OrcamentoComItens
import br.com.tiago.obramaster.server.routes.PedidoCompraComItens
import br.com.tiago.obramaster.server.routes.TokenResponse
import br.com.tiago.obramaster.server.routes.VendaComParcelas
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
class ComprasOrcamentosVendasRoutesTest {

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
    fun pedidoCompra_criaComItensEAtualizaSubstituindoALista() = testApplication {
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
            setBody(Projeto(id = projetoId, nome = "Obra", orcamentoTotal = 100_000_00L, status = StatusProjeto.PLANEJAMENTO))
        }

        val pedido = PedidoCompra(id = uuid(), projetoId = projetoId, data = 0L, status = StatusPedidoCompra.COTACAO, valorTotal = 32_000L)
        val item1 = ItemCompra(id = uuid(), pedidoId = pedido.id, materialId = uuid(), quantidade = 10.0, unidade = "sc", valorUnitario = 3_200L, valorTotal = 32_000L)
        val criado = client.post("/pedidos-compra") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(PedidoCompraComItens(pedido, listOf(item1)))
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val buscado = client.get("/pedidos-compra/${pedido.id}") { bearerAuth(token.accessToken) }.body<PedidoCompraComItens>()
        assertEquals(1, buscado.itens.size)

        // Atualiza substituindo por 2 itens diferentes — a lista tem que refletir exatamente isso, não acumular com o item antigo.
        val item2 = ItemCompra(id = uuid(), pedidoId = pedido.id, materialId = uuid(), quantidade = 5.0, unidade = "un", valorUnitario = 1_000L, valorTotal = 5_000L)
        val item3 = ItemCompra(id = uuid(), pedidoId = pedido.id, materialId = uuid(), quantidade = 2.0, unidade = "un", valorUnitario = 500L, valorTotal = 1_000L)
        val pedidoAtualizado = pedido.copy(status = StatusPedidoCompra.APROVADO, valorTotal = 6_000L)
        client.put("/pedidos-compra/${pedido.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(PedidoCompraComItens(pedidoAtualizado, listOf(item2, item3)))
        }

        val depois = client.get("/pedidos-compra/${pedido.id}") { bearerAuth(token.accessToken) }.body<PedidoCompraComItens>()
        assertEquals(2, depois.itens.size)
        assertEquals(StatusPedidoCompra.APROVADO, depois.pedido.status)
        assertTrue(depois.itens.none { it.id == item1.id })
    }

    @Test
    fun orcamento_criaComConfigBdiEItensDeMaterialEMaoDeObra() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val configBdi = ConfigBDI(
            id = uuid(), nome = "Padrão", administracaoCentral = 0.05, seguroGarantia = 0.01,
            riscos = 0.01, despesasFinanceiras = 0.01, lucro = 0.08, tributos = 0.1, padrao = true,
        )
        client.post("/configs-bdi") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(configBdi)
        }

        val orcamento = Orcamento(id = uuid(), titulo = "Reforma Cozinha", data = 0L, validadeDias = 30, configBdiId = configBdi.id)
        val itemMaterial = ItemOrcamento(id = uuid(), orcamentoId = orcamento.id, tipo = TipoItemOrcamento.MATERIAL, descricao = "Cimento", quantidade = 10.0, unidade = "sc", valorUnitario = 3_200L, valorTotal = 32_000L)
        val itemMaoDeObra = ItemOrcamento(id = uuid(), orcamentoId = orcamento.id, tipo = TipoItemOrcamento.MAO_DE_OBRA, descricao = "Pedreiro", quantidade = 5.0, unidade = "diária", valorUnitario = 15_000L, valorTotal = 75_000L)

        val criado = client.post("/orcamentos") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(OrcamentoComItens(orcamento, listOf(itemMaterial, itemMaoDeObra)))
        }
        assertEquals(HttpStatusCode.Created, criado.status)

        val lista = client.get("/orcamentos") { bearerAuth(token.accessToken) }.body<List<OrcamentoComItens>>()
        val encontrado = lista.single { it.orcamento.id == orcamento.id }
        assertEquals(2, encontrado.itens.size)
        assertEquals(configBdi.id, encontrado.orcamento.configBdiId)
    }

    @Test
    fun venda_criaComParcelasEMarcaUmaComoPaga() = testApplication {
        setupBanco()
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val empresaId = uuid()
        seedGestor(empresaId)
        val token = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("gestor", "senha123", empresaId))
        }.body<TokenResponse>()

        val clienteId = uuid()
        val venda = Venda(id = uuid(), clientePessoaId = clienteId, descricao = "Serviço de acabamento", valorTotal = 20_000L, data = 0L, formaPagamento = "Boleto", status = StatusVenda.NEGOCIACAO)
        val parcela1 = ParcelaVenda(id = uuid(), vendaId = venda.id, numero = 1, valor = 10_000L, vencimento = 0L, pago = false)
        val parcela2 = ParcelaVenda(id = uuid(), vendaId = venda.id, numero = 2, valor = 10_000L, vencimento = 1L, pago = false)

        client.post("/vendas") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(VendaComParcelas(venda, listOf(parcela1, parcela2)))
        }

        val vendaFechada = venda.copy(status = StatusVenda.FECHADA)
        val parcela1Paga = parcela1.copy(pago = true)
        client.put("/vendas/${venda.id}") {
            bearerAuth(token.accessToken)
            contentType(ContentType.Application.Json)
            setBody(VendaComParcelas(vendaFechada, listOf(parcela1Paga, parcela2)))
        }

        val buscada = client.get("/vendas/${venda.id}") { bearerAuth(token.accessToken) }.body<VendaComParcelas>()
        assertEquals(StatusVenda.FECHADA, buscada.venda.status)
        assertTrue(buscada.parcelas.single { it.numero == 1 }.pago)
        assertTrue(buscada.parcelas.single { it.numero == 2 }.pago.not())
    }
}
