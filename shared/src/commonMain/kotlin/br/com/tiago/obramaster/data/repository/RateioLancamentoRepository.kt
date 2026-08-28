package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.RateioLancamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RateioLancamentoRepository {
    suspend fun listarDoLancamento(lancamentoId: String): List<RateioLancamento>

    /** Substitui todo o rateio de um lançamento (apaga o anterior e insere os novos). */
    suspend fun substituir(lancamentoId: String, rateios: List<RateioLancamento>)
}

class SqlDelightRateioLancamentoRepository(
    private val db: ObraMasterDatabase,
) : RateioLancamentoRepository {
    private val queries = db.rateioLancamentoQueries

    override suspend fun listarDoLancamento(lancamentoId: String): List<RateioLancamento> = withContext(Dispatchers.Default) {
        queries.selectDoLancamento(lancamentoId).executeAsList().map { it.toDomain() }
    }

    override suspend fun substituir(lancamentoId: String, rateios: List<RateioLancamento>) {
        withContext(Dispatchers.Default) {
            queries.deleteDoLancamento(lancamentoId)
            rateios.forEach { rateio ->
                queries.insert(id = rateio.id, lancamentoId = rateio.lancamentoId, centroDeCustoId = rateio.centroDeCustoId, percentual = rateio.percentual)
            }
        }
    }
}

private fun br.com.tiago.obramaster.db.RateioLancamento.toDomain() = RateioLancamento(
    id = id,
    lancamentoId = lancamentoId,
    centroDeCustoId = centroDeCustoId,
    percentual = percentual,
)
