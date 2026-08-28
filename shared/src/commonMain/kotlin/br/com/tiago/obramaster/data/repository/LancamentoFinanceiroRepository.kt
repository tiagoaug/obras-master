package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface LancamentoFinanceiroRepository {
    suspend fun listarAtivos(): List<LancamentoFinanceiro>
    suspend fun salvar(lancamento: LancamentoFinanceiro)
    suspend fun atualizar(lancamento: LancamentoFinanceiro)
    suspend fun marcarPago(id: String, pago: Boolean)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<LancamentoFinanceiro>>
}

class SqlDelightLancamentoFinanceiroRepository(
    private val db: ObraMasterDatabase,
) : LancamentoFinanceiroRepository {
    private val queries = db.lancamentoFinanceiroQueries

    override suspend fun listarAtivos(): List<LancamentoFinanceiro> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(lancamento: LancamentoFinanceiro) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = lancamento.id,
                tipo = lancamento.tipo.name,
                categoriaId = lancamento.categoriaId,
                centroDeCustoId = lancamento.centroDeCustoId,
                natureza = lancamento.natureza.name,
                projetoId = lancamento.projetoId,
                etapaId = lancamento.etapaId,
                descricao = lancamento.descricao,
                valor = lancamento.valor,
                data_ = lancamento.data,
                formaPagamento = lancamento.formaPagamento,
                pago = lancamento.pago,
                pessoaId = lancamento.pessoaId,
                anexoUri = lancamento.anexoUri,
                contaId = lancamento.contaId,
                ativo = lancamento.ativo,
            )
        }
    }

    override suspend fun atualizar(lancamento: LancamentoFinanceiro) {
        withContext(Dispatchers.Default) {
            queries.update(
                tipo = lancamento.tipo.name,
                categoriaId = lancamento.categoriaId,
                centroDeCustoId = lancamento.centroDeCustoId,
                natureza = lancamento.natureza.name,
                projetoId = lancamento.projetoId,
                etapaId = lancamento.etapaId,
                descricao = lancamento.descricao,
                valor = lancamento.valor,
                data_ = lancamento.data,
                formaPagamento = lancamento.formaPagamento,
                pago = lancamento.pago,
                pessoaId = lancamento.pessoaId,
                anexoUri = lancamento.anexoUri,
                contaId = lancamento.contaId,
                id = lancamento.id,
            )
        }
    }

    override suspend fun marcarPago(id: String, pago: Boolean) {
        withContext(Dispatchers.Default) { queries.marcarPago(pago, id) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivos(): Flow<List<LancamentoFinanceiro>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.LancamentoFinanceiro.toDomain() = LancamentoFinanceiro(
    id = id,
    tipo = TipoLancamento.valueOf(tipo),
    categoriaId = categoriaId,
    centroDeCustoId = centroDeCustoId,
    natureza = NaturezaLancamento.valueOf(natureza),
    projetoId = projetoId,
    etapaId = etapaId,
    descricao = descricao,
    valor = valor,
    data = data_,
    formaPagamento = formaPagamento,
    pago = pago,
    pessoaId = pessoaId,
    anexoUri = anexoUri,
    contaId = contaId,
    ativo = ativo,
)
