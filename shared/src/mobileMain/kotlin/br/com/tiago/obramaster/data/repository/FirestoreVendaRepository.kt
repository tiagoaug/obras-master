package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.Venda
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

/** Mesmo padrão de agregado de FirestorePedidoCompraRepository (venda + parcelas num doc só). */
@Serializable
private data class VendaDoc(val venda: Venda, val parcelas: List<ParcelaVenda> = emptyList())

class FirestoreVendaRepository(empresaContexto: EmpresaContexto) : VendaRepository {
    private val colecao = FirestoreCollection(empresaContexto, "vendas", VendaDoc.serializer())

    override suspend fun listarTodos(): List<Venda> = colecao.listarTodos().map { it.venda }.filter { it.ativo }

    override suspend fun salvar(venda: Venda, parcelas: List<ParcelaVenda>) =
        colecao.salvar(venda.id, VendaDoc(venda, parcelas))

    override suspend fun atualizarStatus(id: String, status: StatusVenda) =
        colecao.atualizarCampos(id, "venda.status" to status.name)

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "venda.ativo" to false)
    override suspend fun parcelasDaVenda(vendaId: String): List<ParcelaVenda> = colecao.buscarPorId(vendaId)?.parcelas.orEmpty()

    // Firestore não atualiza um elemento de array isolado — regrava o array inteiro com a parcela substituída.
    override suspend fun atualizarParcela(parcela: ParcelaVenda) {
        val doc = colecao.buscarPorId(parcela.vendaId) ?: return
        val parcelasAtualizadas = doc.parcelas.map { if (it.id == parcela.id) parcela else it }
        colecao.salvar(parcela.vendaId, doc.copy(parcelas = parcelasAtualizadas))
    }

    override fun observarTodos(): Flow<List<Venda>> =
        colecao.observarTodos().map { lista -> lista.map { it.venda }.filter { it.ativo } }
}
