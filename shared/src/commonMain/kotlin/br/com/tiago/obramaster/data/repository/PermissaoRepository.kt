package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Permissao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PermissaoRepository {
    suspend fun listarTodas(): List<Permissao>
    suspend fun listarPorColaborador(colaboradorId: String): List<Permissao>
    suspend fun definir(colaboradorId: String, moduleId: String, nivel: NivelPermissao)
    suspend fun removerTodasDoColaborador(colaboradorId: String)
    fun observarTodas(): Flow<List<Permissao>>
}

class SqlDelightPermissaoRepository(
    private val db: ObraMasterDatabase,
) : PermissaoRepository {
    private val queries = db.permissaoQueries

    override suspend fun listarTodas(): List<Permissao> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun listarPorColaborador(colaboradorId: String): List<Permissao> = withContext(Dispatchers.Default) {
        queries.selectByColaborador(colaboradorId).executeAsList().map { it.toDomain() }
    }

    override suspend fun definir(colaboradorId: String, moduleId: String, nivel: NivelPermissao) {
        withContext(Dispatchers.Default) {
            queries.upsert(colaboradorId = colaboradorId, moduleId = moduleId, nivel = nivel.name)
        }
    }

    override suspend fun removerTodasDoColaborador(colaboradorId: String) {
        withContext(Dispatchers.Default) {
            queries.deleteByColaborador(colaboradorId)
        }
    }

    override fun observarTodas(): Flow<List<Permissao>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Permissao.toDomain() = Permissao(
    colaboradorId = colaboradorId,
    moduleId = moduleId,
    nivel = NivelPermissao.valueOf(nivel),
)
