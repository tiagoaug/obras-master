package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.Venda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface VendaRepository {
    suspend fun listarTodos(): List<Venda>

    /** [parcelas] é salvo junto — mesmo padrão de substituição total já usado em PedidoCompra/Orcamento. */
    suspend fun salvar(venda: Venda, parcelas: List<ParcelaVenda>)
    suspend fun atualizarStatus(id: String, status: StatusVenda)
    suspend fun desativar(id: String)
    suspend fun parcelasDaVenda(vendaId: String): List<ParcelaVenda>
    suspend fun atualizarParcela(parcela: ParcelaVenda)
    fun observarTodos(): Flow<List<Venda>>
}

class SqlDelightVendaRepository(
    private val db: ObraMasterDatabase,
) : VendaRepository {
    private val queries = db.vendaQueries

    override suspend fun listarTodos(): List<Venda> = withContext(Dispatchers.Default) {
        queries.selectTodosAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(venda: Venda, parcelas: List<ParcelaVenda>) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insertVenda(
                    id = venda.id,
                    projetoId = venda.projetoId,
                    clientePessoaId = venda.clientePessoaId,
                    descricao = venda.descricao,
                    valorTotal = venda.valorTotal,
                    data_ = venda.data,
                    formaPagamento = venda.formaPagamento,
                    status = venda.status.name,
                    ativo = venda.ativo,
                )
                queries.deleteParcelasDaVenda(venda.id)
                parcelas.forEach { parcela ->
                    queries.insertParcela(
                        id = parcela.id,
                        vendaId = parcela.vendaId,
                        numero = parcela.numero.toLong(),
                        valor = parcela.valor,
                        vencimento = parcela.vencimento,
                        pago = parcela.pago,
                        lancamentoFinanceiroId = parcela.lancamentoFinanceiroId,
                    )
                }
            }
        }
    }

    override suspend fun atualizarStatus(id: String, status: StatusVenda) {
        withContext(Dispatchers.Default) { queries.updateStatus(status.name, id) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDeleteVenda(id) }
    }

    override suspend fun parcelasDaVenda(vendaId: String): List<ParcelaVenda> = withContext(Dispatchers.Default) {
        queries.selectParcelasDaVenda(vendaId).executeAsList().map { it.toDomain() }
    }

    override suspend fun atualizarParcela(parcela: ParcelaVenda) {
        withContext(Dispatchers.Default) { queries.updateParcela(parcela.pago, parcela.lancamentoFinanceiroId, parcela.id) }
    }

    override fun observarTodos(): Flow<List<Venda>> =
        queries.selectTodosAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Venda.toDomain() = Venda(
    id = id,
    projetoId = projetoId,
    clientePessoaId = clientePessoaId,
    descricao = descricao,
    valorTotal = valorTotal,
    data = data_,
    formaPagamento = formaPagamento,
    status = StatusVenda.valueOf(status),
    ativo = ativo,
)

private fun br.com.tiago.obramaster.db.ParcelaVenda.toDomain() = ParcelaVenda(
    id = id,
    vendaId = vendaId,
    numero = numero.toInt(),
    valor = valor,
    vencimento = vencimento,
    pago = pago,
    lancamentoFinanceiroId = lancamentoFinanceiroId,
)
