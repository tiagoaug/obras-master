package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ModuleConfigRepository {
    suspend fun listarTodos(): Map<String, Boolean>
    suspend fun definir(moduleId: String, enabled: Boolean)
    fun observarTodos(): Flow<Map<String, Boolean>>
}

class SqlDelightModuleConfigRepository(
    private val db: ObraMasterDatabase,
) : ModuleConfigRepository {
    private val queries = db.moduleConfigQueries

    override suspend fun listarTodos(): Map<String, Boolean> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().associate { it.moduleId to it.enabled }
    }

    override suspend fun definir(moduleId: String, enabled: Boolean) {
        withContext(Dispatchers.Default) {
            queries.upsert(moduleId = moduleId, enabled = enabled)
        }
    }

    override fun observarTodos(): Flow<Map<String, Boolean>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.associate { it.moduleId to it.enabled } }
}
