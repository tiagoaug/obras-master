package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface CentroDeCustoRepository {
    suspend fun listarAtivos(): List<CentroDeCusto>
    suspend fun buscarPorProjetoId(projetoId: String): CentroDeCusto?
    suspend fun salvar(centro: CentroDeCusto)
    suspend fun atualizar(centro: CentroDeCusto)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<CentroDeCusto>>
}

class SqlDelightCentroDeCustoRepository(
    private val db: ObraMasterDatabase,
) : CentroDeCustoRepository {
    private val queries = db.centroDeCustoQueries

    override suspend fun listarAtivos(): List<CentroDeCusto> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun buscarPorProjetoId(projetoId: String): CentroDeCusto? = withContext(Dispatchers.Default) {
        queries.selectPorProjetoId(projetoId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun salvar(centro: CentroDeCusto) {
        withContext(Dispatchers.Default) {
            queries.insert(id = centro.id, nome = centro.nome, tipo = centro.tipo.name, projetoId = centro.projetoId, ativo = centro.ativo)
        }
    }

    override suspend fun atualizar(centro: CentroDeCusto) {
        withContext(Dispatchers.Default) { queries.update(nome = centro.nome, tipo = centro.tipo.name, id = centro.id) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivos(): Flow<List<CentroDeCusto>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.CentroDeCusto.toDomain() = CentroDeCusto(
    id = id,
    nome = nome,
    tipo = TipoCentroDeCusto.valueOf(tipo),
    projetoId = projetoId,
    ativo = ativo,
)
