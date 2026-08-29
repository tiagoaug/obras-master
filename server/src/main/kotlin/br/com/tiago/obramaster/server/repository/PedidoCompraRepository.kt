package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import br.com.tiago.obramaster.server.db.ItensCompra
import br.com.tiago.obramaster.server.db.PedidosCompra
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/** Retorna sempre `Pair<PedidoCompra, List<ItemCompra>>` — a spec trata o pedido e seus itens como
 * um agregado só (ver ItemCompra.pedidoId), mesmo padrão de Equipe/EquipeMembros. */
object PedidoCompraRepository {

    private fun itensDe(pedidoId: String): List<ItemCompra> =
        ItensCompra.selectAll().andWhere { ItensCompra.pedidoId eq pedidoId }.map(::rowToItem)

    private fun rowToItem(row: ResultRow) = ItemCompra(
        id = row[ItensCompra.id],
        pedidoId = row[ItensCompra.pedidoId],
        materialId = row[ItensCompra.materialId],
        quantidade = row[ItensCompra.quantidade],
        unidade = row[ItensCompra.unidade],
        valorUnitario = row[ItensCompra.valorUnitario],
        valorTotal = row[ItensCompra.valorTotal],
    )

    private fun rowToPedido(row: ResultRow) = PedidoCompra(
        id = row[PedidosCompra.id],
        projetoId = row[PedidosCompra.projetoId],
        etapaId = row[PedidosCompra.etapaId],
        fornecedorId = row[PedidosCompra.fornecedorId],
        data = row[PedidosCompra.data],
        status = StatusPedidoCompra.valueOf(row[PedidosCompra.status]),
        valorTotal = row[PedidosCompra.valorTotal],
        lancamentoFinanceiroId = row[PedidosCompra.lancamentoFinanceiroId],
        ativo = row[PedidosCompra.ativo],
    )

    private fun substituirItens(pedidoId: String, itens: List<ItemCompra>) {
        ItensCompra.deleteWhere { ItensCompra.pedidoId eq pedidoId }
        if (itens.isNotEmpty()) {
            ItensCompra.batchInsert(itens) { item ->
                this[ItensCompra.id] = item.id
                this[ItensCompra.pedidoId] = pedidoId
                this[ItensCompra.materialId] = item.materialId
                this[ItensCompra.quantidade] = item.quantidade
                this[ItensCompra.unidade] = item.unidade
                this[ItensCompra.valorUnitario] = item.valorUnitario
                this[ItensCompra.valorTotal] = item.valorTotal
            }
        }
    }

    fun listar(empresaId: String): List<Pair<PedidoCompra, List<ItemCompra>>> = transaction {
        PedidosCompra.selectAll()
            .andWhere { PedidosCompra.empresaId eq empresaId }
            .andWhere { PedidosCompra.deletedAt.isNull() }
            .map { row -> rowToPedido(row) to itensDe(row[PedidosCompra.id]) }
    }

    fun buscarPorId(empresaId: String, id: String): Pair<PedidoCompra, List<ItemCompra>>? = transaction {
        PedidosCompra.selectAll()
            .andWhere { PedidosCompra.id eq id }
            .andWhere { PedidosCompra.empresaId eq empresaId }
            .andWhere { PedidosCompra.deletedAt.isNull() }
            .map { row -> rowToPedido(row) to itensDe(row[PedidosCompra.id]) }
            .singleOrNull()
    }

    fun criar(empresaId: String, pedido: PedidoCompra, itens: List<ItemCompra>): Pair<PedidoCompra, List<ItemCompra>> = transaction {
        PedidosCompra.insert {
            it[id] = pedido.id
            it[PedidosCompra.empresaId] = empresaId
            it[projetoId] = pedido.projetoId
            it[etapaId] = pedido.etapaId
            it[fornecedorId] = pedido.fornecedorId
            it[data] = pedido.data
            it[status] = pedido.status.name
            it[valorTotal] = pedido.valorTotal
            it[lancamentoFinanceiroId] = pedido.lancamentoFinanceiroId
            it[ativo] = pedido.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        substituirItens(pedido.id, itens)
        pedido to itens
    }

    fun atualizar(empresaId: String, pedido: PedidoCompra, itens: List<ItemCompra>): Boolean = transaction {
        val linhas = PedidosCompra.update({ (PedidosCompra.id eq pedido.id) and (PedidosCompra.empresaId eq empresaId) }) {
            it[etapaId] = pedido.etapaId
            it[fornecedorId] = pedido.fornecedorId
            it[data] = pedido.data
            it[status] = pedido.status.name
            it[valorTotal] = pedido.valorTotal
            it[lancamentoFinanceiroId] = pedido.lancamentoFinanceiroId
            it[ativo] = pedido.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        if (linhas > 0) substituirItens(pedido.id, itens)
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = PedidosCompra.update({ (PedidosCompra.id eq id) and (PedidosCompra.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
