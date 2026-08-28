package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.TipoMovimentoConta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface MovimentoContaRepository {
    suspend fun listarDaConta(contaId: String): List<MovimentoConta>
    suspend fun listarTodos(): List<MovimentoConta>
    suspend fun salvar(movimento: MovimentoConta)
    suspend fun marcarConciliado(id: String, conciliado: Boolean)
    suspend fun excluir(id: String)

    /** Remove o movimento gerado automaticamente por um lançamento (ex.: ao desmarcar "pago"). */
    suspend fun excluirPorLancamentoId(lancamentoId: String)
    fun observarDaConta(contaId: String): Flow<List<MovimentoConta>>
    fun observarTodos(): Flow<List<MovimentoConta>>
}

class SqlDelightMovimentoContaRepository(
    private val db: ObraMasterDatabase,
) : MovimentoContaRepository {
    private val queries = db.movimentoContaQueries

    override suspend fun listarDaConta(contaId: String): List<MovimentoConta> = withContext(Dispatchers.Default) {
        queries.selectDaConta(contaId).executeAsList().map { it.toDomain() }
    }

    override suspend fun listarTodos(): List<MovimentoConta> = withContext(Dispatchers.Default) {
        queries.selectTodos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(movimento: MovimentoConta) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = movimento.id,
                contaId = movimento.contaId,
                tipo = movimento.tipo.name,
                valor = movimento.valor,
                data_ = movimento.data,
                descricao = movimento.descricao,
                lancamentoFinanceiroId = movimento.lancamentoFinanceiroId,
                transferenciaVinculoId = movimento.transferenciaVinculoId,
                conciliado = movimento.conciliado,
            )
        }
    }

    override suspend fun marcarConciliado(id: String, conciliado: Boolean) {
        withContext(Dispatchers.Default) { queries.marcarConciliado(conciliado, id) }
    }

    override suspend fun excluir(id: String) {
        withContext(Dispatchers.Default) { queries.delete(id) }
    }

    override suspend fun excluirPorLancamentoId(lancamentoId: String) {
        withContext(Dispatchers.Default) { queries.deletePorLancamentoId(lancamentoId) }
    }

    override fun observarDaConta(contaId: String): Flow<List<MovimentoConta>> =
        queries.selectDaConta(contaId).asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }

    override fun observarTodos(): Flow<List<MovimentoConta>> =
        queries.selectTodos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.MovimentoConta.toDomain() = MovimentoConta(
    id = id,
    contaId = contaId,
    tipo = TipoMovimentoConta.valueOf(tipo),
    valor = valor,
    data = data_,
    descricao = descricao,
    lancamentoFinanceiroId = lancamentoFinanceiroId,
    transferenciaVinculoId = transferenciaVinculoId,
    conciliado = conciliado,
)
