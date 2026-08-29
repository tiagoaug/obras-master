package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.StatusOrcamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

/** Mesmo padrão de agregado de FirestorePedidoCompraRepository (orçamento + itens num doc só). */
@Serializable
private data class OrcamentoDoc(val orcamento: Orcamento, val itens: List<ItemOrcamento> = emptyList())

class FirestoreOrcamentoRepository(empresaContexto: EmpresaContexto) : OrcamentoRepository {
    private val colecao = FirestoreCollection(empresaContexto, "orcamentos", OrcamentoDoc.serializer())

    override suspend fun listarTodos(): List<Orcamento> = colecao.listarTodos().map { it.orcamento }.filter { it.ativo }

    override suspend fun salvar(orcamento: Orcamento, itens: List<ItemOrcamento>) =
        colecao.salvar(orcamento.id, OrcamentoDoc(orcamento, itens))

    override suspend fun atualizarStatus(id: String, status: StatusOrcamento) =
        colecao.atualizarCampos(id, "orcamento.status" to status.name)

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "orcamento.ativo" to false)
    override suspend fun itensDoOrcamento(orcamentoId: String): List<ItemOrcamento> = colecao.buscarPorId(orcamentoId)?.itens.orEmpty()

    override fun observarTodos(): Flow<List<Orcamento>> =
        colecao.observarTodos().map { lista -> lista.map { it.orcamento }.filter { it.ativo } }
}
