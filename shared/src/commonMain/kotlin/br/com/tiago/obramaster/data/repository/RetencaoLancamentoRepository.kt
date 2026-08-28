package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.RetencaoLancamento
import br.com.tiago.obramaster.domain.TipoRetencao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RetencaoLancamentoRepository {
    suspend fun listarDoLancamento(lancamentoId: String): List<RetencaoLancamento>

    /** Substitui toda a lista de retenções de um lançamento (apaga a anterior e insere as novas). */
    suspend fun substituir(lancamentoId: String, retencoes: List<RetencaoLancamento>)
}

class SqlDelightRetencaoLancamentoRepository(
    private val db: ObraMasterDatabase,
) : RetencaoLancamentoRepository {
    private val queries = db.retencaoLancamentoQueries

    override suspend fun listarDoLancamento(lancamentoId: String): List<RetencaoLancamento> = withContext(Dispatchers.Default) {
        queries.selectDoLancamento(lancamentoId).executeAsList().map { it.toDomain() }
    }

    override suspend fun substituir(lancamentoId: String, retencoes: List<RetencaoLancamento>) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.deleteDoLancamento(lancamentoId)
                retencoes.forEach { retencao ->
                    queries.insert(
                        id = retencao.id,
                        lancamentoId = retencao.lancamentoId,
                        tipo = retencao.tipo.name,
                        percentual = retencao.percentual,
                        valorCalculado = retencao.valorCalculado,
                    )
                }
            }
        }
    }
}

private fun br.com.tiago.obramaster.db.RetencaoLancamento.toDomain() = RetencaoLancamento(
    id = id,
    lancamentoId = lancamentoId,
    tipo = TipoRetencao.valueOf(tipo),
    percentual = percentual,
    valorCalculado = valorCalculado,
)
