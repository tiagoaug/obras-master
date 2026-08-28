package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.PontoXY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ComodoRepository {
    suspend fun listarDaPlanta(plantaId: String): List<Comodo>
    suspend fun salvar(comodo: Comodo)
    suspend fun renomear(id: String, nome: String)
    suspend fun desativar(id: String)
    fun observarDaPlanta(plantaId: String): Flow<List<Comodo>>
}

class SqlDelightComodoRepository(
    private val db: ObraMasterDatabase,
) : ComodoRepository {
    private val queries = db.comodoQueries
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarDaPlanta(plantaId: String): List<Comodo> = withContext(Dispatchers.Default) {
        queries.selectAtivosDaPlanta(plantaId).executeAsList().map { it.toDomain(json) }
    }

    override suspend fun salvar(comodo: Comodo) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = comodo.id,
                plantaId = comodo.plantaId,
                nome = comodo.nome,
                pontosJson = json.encodeToString(comodo.pontos),
                corPreenchimento = comodo.corPreenchimento,
                areaM2 = comodo.areaM2,
                perimetroM = comodo.perimetroM,
                ativo = comodo.ativo,
            )
        }
    }

    override suspend fun renomear(id: String, nome: String) {
        withContext(Dispatchers.Default) { queries.renomear(nome, id) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarDaPlanta(plantaId: String): Flow<List<Comodo>> =
        queries.selectAtivosDaPlanta(plantaId).asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain(json) } }
}

private fun br.com.tiago.obramaster.db.Comodo.toDomain(json: Json) = Comodo(
    id = id,
    plantaId = plantaId,
    nome = nome,
    pontos = json.decodeFromString<List<PontoXY>>(pontosJson),
    corPreenchimento = corPreenchimento,
    areaM2 = areaM2,
    perimetroM = perimetroM,
    ativo = ativo,
)
