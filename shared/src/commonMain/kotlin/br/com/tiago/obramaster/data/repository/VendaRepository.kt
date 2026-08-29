package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.Venda
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreVendaRepository (mobileMain). */
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
