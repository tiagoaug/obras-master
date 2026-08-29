package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

/** Fase 10 (pivô Firebase) — pedido + itens viram um documento só (`pedidosCompra/{id}`), com
 * `itens` embutido como array; no Ktor precisou de tabela filha porque SQL não tem array
 * (ver server/.../PedidoCompraRepository.kt), aqui não precisa. */
@Serializable
private data class PedidoCompraDoc(val pedido: PedidoCompra, val itens: List<ItemCompra> = emptyList())

class FirestorePedidoCompraRepository(empresaContexto: EmpresaContexto) : PedidoCompraRepository {
    private val colecao = FirestoreCollection(empresaContexto, "pedidosCompra", PedidoCompraDoc.serializer())

    override suspend fun listarDoProjeto(projetoId: String): List<PedidoCompra> =
        colecao.consultar { where { "pedido.projetoId" equalTo projetoId } }.map { it.pedido }.filter { it.ativo }

    override suspend fun listarTodos(): List<PedidoCompra> = colecao.listarTodos().map { it.pedido }.filter { it.ativo }

    override suspend fun salvar(pedido: PedidoCompra, itens: List<ItemCompra>) =
        colecao.salvar(pedido.id, PedidoCompraDoc(pedido, itens))

    override suspend fun atualizarStatus(id: String, status: StatusPedidoCompra, lancamentoFinanceiroId: String?) {
        colecao.atualizarCampos(id, "pedido.status" to status.name, "pedido.lancamentoFinanceiroId" to lancamentoFinanceiroId)
    }

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "pedido.ativo" to false)

    override suspend fun itensDoPedido(pedidoId: String): List<ItemCompra> = colecao.buscarPorId(pedidoId)?.itens.orEmpty()

    override suspend fun itensDeTodos(): List<ItemCompra> = colecao.listarTodos().flatMap { it.itens }

    override fun observarTodos(): Flow<List<PedidoCompra>> =
        colecao.observarTodos().map { lista -> lista.map { it.pedido }.filter { it.ativo } }
}
