package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PedidoCompraRepository {
    suspend fun listarDoProjeto(projetoId: String): List<PedidoCompra>
    suspend fun listarTodos(): List<PedidoCompra>

    /** [itens] é salvo junto — mesmo padrão de substituição total já usado em RateioLancamento. */
    suspend fun salvar(pedido: PedidoCompra, itens: List<ItemCompra>)
    suspend fun atualizarStatus(id: String, status: StatusPedidoCompra, lancamentoFinanceiroId: String?)
    suspend fun desativar(id: String)
    suspend fun itensDoPedido(pedidoId: String): List<ItemCompra>
    suspend fun itensDeTodos(): List<ItemCompra>
    fun observarTodos(): Flow<List<PedidoCompra>>
}

class SqlDelightPedidoCompraRepository(
    private val db: ObraMasterDatabase,
) : PedidoCompraRepository {
    private val queries = db.pedidoCompraQueries

    override suspend fun listarDoProjeto(projetoId: String): List<PedidoCompra> = withContext(Dispatchers.Default) {
        queries.selectAtivosDoProjeto(projetoId).executeAsList().map { it.toDomain() }
    }

    override suspend fun listarTodos(): List<PedidoCompra> = withContext(Dispatchers.Default) {
        queries.selectTodosAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(pedido: PedidoCompra, itens: List<ItemCompra>) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insertPedido(
                    id = pedido.id,
                    projetoId = pedido.projetoId,
                    etapaId = pedido.etapaId,
                    fornecedorId = pedido.fornecedorId,
                    data_ = pedido.data,
                    status = pedido.status.name,
                    valorTotal = pedido.valorTotal,
                    lancamentoFinanceiroId = pedido.lancamentoFinanceiroId,
                    ativo = pedido.ativo,
                )
                queries.deleteItensDoPedido(pedido.id)
                itens.forEach { item ->
                    queries.insertItem(
                        id = item.id,
                        pedidoId = item.pedidoId,
                        materialId = item.materialId,
                        quantidade = item.quantidade,
                        unidade = item.unidade,
                        valorUnitario = item.valorUnitario,
                        valorTotal = item.valorTotal,
                    )
                }
            }
        }
    }

    override suspend fun atualizarStatus(id: String, status: StatusPedidoCompra, lancamentoFinanceiroId: String?) {
        withContext(Dispatchers.Default) { queries.updateStatus(status.name, lancamentoFinanceiroId, id) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDeletePedido(id) }
    }

    override suspend fun itensDoPedido(pedidoId: String): List<ItemCompra> = withContext(Dispatchers.Default) {
        queries.selectItensDoPedido(pedidoId).executeAsList().map { it.toDomain() }
    }

    override suspend fun itensDeTodos(): List<ItemCompra> = withContext(Dispatchers.Default) {
        queries.selectTodosItens().executeAsList().map { it.toDomain() }
    }

    override fun observarTodos(): Flow<List<PedidoCompra>> =
        queries.selectTodosAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.PedidoCompra.toDomain() = PedidoCompra(
    id = id,
    projetoId = projetoId,
    etapaId = etapaId,
    fornecedorId = fornecedorId,
    data = data_,
    status = StatusPedidoCompra.valueOf(status),
    valorTotal = valorTotal,
    lancamentoFinanceiroId = lancamentoFinanceiroId,
    ativo = ativo,
)

private fun br.com.tiago.obramaster.db.ItemCompra.toDomain() = ItemCompra(
    id = id,
    pedidoId = pedidoId,
    materialId = materialId,
    quantidade = quantidade,
    unidade = unidade,
    valorUnitario = valorUnitario,
    valorTotal = valorTotal,
)
