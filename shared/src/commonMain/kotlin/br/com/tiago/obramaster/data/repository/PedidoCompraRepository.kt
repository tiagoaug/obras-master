package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestorePedidoCompraRepository (mobileMain). */
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
