package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.StatusOrcamento
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreOrcamentoRepository (mobileMain). */
interface OrcamentoRepository {
    suspend fun listarTodos(): List<Orcamento>

    /** [itens] é salvo junto — mesmo padrão de substituição total já usado em PedidoCompra/ItemCompra. */
    suspend fun salvar(orcamento: Orcamento, itens: List<ItemOrcamento>)
    suspend fun atualizarStatus(id: String, status: StatusOrcamento)
    suspend fun desativar(id: String)
    suspend fun itensDoOrcamento(orcamentoId: String): List<ItemOrcamento>
    fun observarTodos(): Flow<List<Orcamento>>
}
