package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.StatusOrcamento
import br.com.tiago.obramaster.domain.TipoItemOrcamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface OrcamentoRepository {
    suspend fun listarTodos(): List<Orcamento>

    /** [itens] é salvo junto — mesmo padrão de substituição total já usado em PedidoCompra/ItemCompra. */
    suspend fun salvar(orcamento: Orcamento, itens: List<ItemOrcamento>)
    suspend fun atualizarStatus(id: String, status: StatusOrcamento)
    suspend fun desativar(id: String)
    suspend fun itensDoOrcamento(orcamentoId: String): List<ItemOrcamento>
    fun observarTodos(): Flow<List<Orcamento>>
}

class SqlDelightOrcamentoRepository(
    private val db: ObraMasterDatabase,
) : OrcamentoRepository {
    private val queries = db.orcamentoQueries

    override suspend fun listarTodos(): List<Orcamento> = withContext(Dispatchers.Default) {
        queries.selectTodosAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(orcamento: Orcamento, itens: List<ItemOrcamento>) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insertOrcamento(
                    id = orcamento.id,
                    projetoId = orcamento.projetoId,
                    clientePessoaId = orcamento.clientePessoaId,
                    titulo = orcamento.titulo,
                    data_ = orcamento.data,
                    validadeDias = orcamento.validadeDias.toLong(),
                    status = orcamento.status.name,
                    descontoPercent = orcamento.descontoPercent,
                    observacoes = orcamento.observacoes,
                    configBdiId = orcamento.configBdiId,
                    bdiPercentualCalculado = orcamento.bdiPercentualCalculado,
                    bdiCustomizado = orcamento.bdiCustomizado,
                    custoDiretoTotal = orcamento.custoDiretoTotal,
                    precoVendaTotal = orcamento.precoVendaTotal,
                    ativo = orcamento.ativo,
                )
                queries.deleteItensDoOrcamento(orcamento.id)
                itens.forEach { item ->
                    queries.insertItem(
                        id = item.id,
                        orcamentoId = item.orcamentoId,
                        tipo = item.tipo.name,
                        descricao = item.descricao,
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

    override suspend fun atualizarStatus(id: String, status: StatusOrcamento) {
        withContext(Dispatchers.Default) { queries.updateStatus(status.name, id) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDeleteOrcamento(id) }
    }

    override suspend fun itensDoOrcamento(orcamentoId: String): List<ItemOrcamento> = withContext(Dispatchers.Default) {
        queries.selectItensDoOrcamento(orcamentoId).executeAsList().map { it.toDomain() }
    }

    override fun observarTodos(): Flow<List<Orcamento>> =
        queries.selectTodosAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Orcamento.toDomain() = Orcamento(
    id = id,
    projetoId = projetoId,
    clientePessoaId = clientePessoaId,
    titulo = titulo,
    data = data_,
    validadeDias = validadeDias.toInt(),
    status = StatusOrcamento.valueOf(status),
    descontoPercent = descontoPercent,
    observacoes = observacoes,
    configBdiId = configBdiId,
    bdiPercentualCalculado = bdiPercentualCalculado,
    bdiCustomizado = bdiCustomizado,
    custoDiretoTotal = custoDiretoTotal,
    precoVendaTotal = precoVendaTotal,
    ativo = ativo,
)

private fun br.com.tiago.obramaster.db.ItemOrcamento.toDomain() = ItemOrcamento(
    id = id,
    orcamentoId = orcamentoId,
    tipo = TipoItemOrcamento.valueOf(tipo),
    descricao = descricao,
    materialId = materialId,
    quantidade = quantidade,
    unidade = unidade,
    valorUnitario = valorUnitario,
    valorTotal = valorTotal,
)
