package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface MaterialRepository {
    suspend fun listarAtivos(): List<Material>
    suspend fun salvar(material: Material)
    suspend fun atualizar(material: Material)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<Material>>
}

class SqlDelightMaterialRepository(
    private val db: ObraMasterDatabase,
) : MaterialRepository {
    private val queries = db.materialQueries

    override suspend fun listarAtivos(): List<Material> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(material: Material) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = material.id,
                nome = material.nome,
                unidadePadrao = material.unidadePadrao,
                precoReferencia = material.precoReferencia,
                categoria = material.categoria,
                corId = material.corId,
                ativo = material.ativo,
            )
        }
    }

    override suspend fun atualizar(material: Material) {
        withContext(Dispatchers.Default) {
            queries.update(
                nome = material.nome,
                unidadePadrao = material.unidadePadrao,
                precoReferencia = material.precoReferencia,
                categoria = material.categoria,
                corId = material.corId,
                id = material.id,
            )
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivos(): Flow<List<Material>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Material.toDomain() = Material(
    id = id,
    nome = nome,
    unidadePadrao = unidadePadrao,
    precoReferencia = precoReferencia,
    categoria = categoria,
    corId = corId,
    ativo = ativo,
)
