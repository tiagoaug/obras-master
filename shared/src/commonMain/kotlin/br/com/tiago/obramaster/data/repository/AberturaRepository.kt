package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Abertura
import br.com.tiago.obramaster.domain.TipoAbertura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface AberturaRepository {
    suspend fun listarDaParede(paredeId: String): List<Abertura>
    suspend fun salvar(abertura: Abertura)
    suspend fun desativar(id: String)
    fun observarTodas(): Flow<List<Abertura>>
}

class SqlDelightAberturaRepository(
    private val db: ObraMasterDatabase,
) : AberturaRepository {
    private val queries = db.aberturaQueries

    override suspend fun listarDaParede(paredeId: String): List<Abertura> = withContext(Dispatchers.Default) {
        queries.selectAtivasDaParede(paredeId).executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(abertura: Abertura) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = abertura.id,
                paredeId = abertura.paredeId,
                tipo = abertura.tipo.name,
                posicaoNaParede = abertura.posicaoNaParede,
                larguraCm = abertura.larguraCm,
                ativo = abertura.ativo,
            )
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarTodas(): Flow<List<Abertura>> =
        queries.selectAtivasDaLista().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Abertura.toDomain() = Abertura(
    id = id,
    paredeId = paredeId,
    tipo = TipoAbertura.valueOf(tipo),
    posicaoNaParede = posicaoNaParede,
    larguraCm = larguraCm,
    ativo = ativo,
)
