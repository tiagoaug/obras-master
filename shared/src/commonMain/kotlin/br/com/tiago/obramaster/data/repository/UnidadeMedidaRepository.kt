package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.UnidadeMedida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface UnidadeMedidaRepository {
    suspend fun listarAtivas(): List<UnidadeMedida>
    suspend fun salvar(unidade: UnidadeMedida)
    suspend fun atualizar(unidade: UnidadeMedida)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<UnidadeMedida>>
}

class SqlDelightUnidadeMedidaRepository(
    private val db: ObraMasterDatabase,
) : UnidadeMedidaRepository {
    private val queries = db.unidadeMedidaQueries

    override suspend fun listarAtivas(): List<UnidadeMedida> = withContext(Dispatchers.Default) {
        queries.selectAtivas().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(unidade: UnidadeMedida) {
        withContext(Dispatchers.Default) {
            queries.insert(unidade.id, unidade.sigla, unidade.nome, unidade.ativo)
        }
    }

    override suspend fun atualizar(unidade: UnidadeMedida) {
        withContext(Dispatchers.Default) {
            queries.update(unidade.sigla, unidade.nome, unidade.id)
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivas(): Flow<List<UnidadeMedida>> =
        queries.selectAtivas().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.UnidadeMedida.toDomain() = UnidadeMedida(
    id = id,
    sigla = sigla,
    nome = nome,
    ativo = ativo,
)
