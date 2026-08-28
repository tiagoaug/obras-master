package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Pagamento
import br.com.tiago.obramaster.domain.StatusPagamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PagamentoRepository {
    suspend fun listarDaPessoa(pessoaId: String): List<Pagamento>
    suspend fun salvar(pagamento: Pagamento)
    fun observarTodos(): Flow<List<Pagamento>>
}

class SqlDelightPagamentoRepository(
    private val db: ObraMasterDatabase,
) : PagamentoRepository {
    private val queries = db.pagamentoQueries

    override suspend fun listarDaPessoa(pessoaId: String): List<Pagamento> = withContext(Dispatchers.Default) {
        queries.selectDaPessoa(pessoaId).executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(pagamento: Pagamento) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = pagamento.id,
                pessoaId = pagamento.pessoaId,
                projetoId = pagamento.projetoId,
                periodo = pagamento.periodo,
                valorTotal = pagamento.valorTotal,
                dataPagamento = pagamento.dataPagamento,
                status = pagamento.status.name,
                comprovanteUri = pagamento.comprovanteUri,
                lancamentoFinanceiroId = pagamento.lancamentoFinanceiroId,
            )
        }
    }

    override fun observarTodos(): Flow<List<Pagamento>> =
        queries.selectTodos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Pagamento.toDomain() = Pagamento(
    id = id,
    pessoaId = pessoaId,
    projetoId = projetoId,
    periodo = periodo,
    valorTotal = valorTotal,
    dataPagamento = dataPagamento,
    status = StatusPagamento.valueOf(status),
    comprovanteUri = comprovanteUri,
    lancamentoFinanceiroId = lancamentoFinanceiroId,
)
