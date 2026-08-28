package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.domain.PontoXY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ParedeRepository {
    suspend fun listarDaPlanta(plantaId: String): List<Parede>
    suspend fun salvar(parede: Parede)
    suspend fun desativar(id: String)
    fun observarDaPlanta(plantaId: String): Flow<List<Parede>>
}

class SqlDelightParedeRepository(
    private val db: ObraMasterDatabase,
) : ParedeRepository {
    private val queries = db.paredeQueries

    override suspend fun listarDaPlanta(plantaId: String): List<Parede> = withContext(Dispatchers.Default) {
        queries.selectAtivasDaPlanta(plantaId).executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(parede: Parede) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = parede.id,
                plantaId = parede.plantaId,
                pontoInicioX = parede.pontoInicio.x,
                pontoInicioY = parede.pontoInicio.y,
                pontoFimX = parede.pontoFim.x,
                pontoFimY = parede.pontoFim.y,
                espessuraCm = parede.espessuraCm,
                estrutural = parede.estrutural,
                ativo = parede.ativo,
            )
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarDaPlanta(plantaId: String): Flow<List<Parede>> =
        queries.selectAtivasDaPlanta(plantaId).asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Parede.toDomain() = Parede(
    id = id,
    plantaId = plantaId,
    pontoInicio = PontoXY(pontoInicioX, pontoInicioY),
    pontoFim = PontoXY(pontoFimX, pontoFimY),
    espessuraCm = espessuraCm,
    estrutural = estrutural,
    ativo = ativo,
)
