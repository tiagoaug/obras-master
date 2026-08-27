package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Cor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface CorRepository {
    suspend fun listarAtivas(): List<Cor>
    suspend fun salvar(cor: Cor)
    suspend fun atualizar(cor: Cor)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Cor>>
}

class SqlDelightCorRepository(
    private val db: ObraMasterDatabase,
) : CorRepository {
    private val queries = db.corQueries

    override suspend fun listarAtivas(): List<Cor> = withContext(Dispatchers.Default) {
        queries.selectAtivas().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(cor: Cor) {
        withContext(Dispatchers.Default) {
            queries.insert(cor.id, cor.nome, cor.hex, cor.codigoFabricante, cor.ativo)
        }
    }

    override suspend fun atualizar(cor: Cor) {
        withContext(Dispatchers.Default) {
            queries.update(cor.nome, cor.hex, cor.codigoFabricante, cor.id)
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivas(): Flow<List<Cor>> =
        queries.selectAtivas().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Cor.toDomain() = Cor(
    id = id,
    nome = nome,
    hex = hex,
    codigoFabricante = codigoFabricante,
    ativo = ativo,
)
