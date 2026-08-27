package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.StatusEtapa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface EtapaRepository {
    suspend fun listarDoProjeto(projetoId: String): List<Etapa>
    suspend fun salvar(etapa: Etapa)
    suspend fun atualizar(etapa: Etapa)
    suspend fun reordenar(etapaId: String, novaOrdem: Int)
    suspend fun desativar(id: String)
    fun observarDoProjeto(projetoId: String): Flow<List<Etapa>>
}

class SqlDelightEtapaRepository(
    private val db: ObraMasterDatabase,
) : EtapaRepository {
    private val queries = db.etapaQueries

    override suspend fun listarDoProjeto(projetoId: String): List<Etapa> = withContext(Dispatchers.Default) {
        queries.selectAtivasDoProjeto(projetoId).executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(etapa: Etapa) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = etapa.id,
                projetoId = etapa.projetoId,
                nome = etapa.nome,
                ordem = etapa.ordem.toLong(),
                orcamentoEtapa = etapa.orcamentoEtapa,
                dataInicio = etapa.dataInicio,
                dataFim = etapa.dataFim,
                progressoPercent = etapa.progressoPercent.toLong(),
                status = etapa.status.name,
                ativo = etapa.ativo,
            )
        }
    }

    override suspend fun atualizar(etapa: Etapa) {
        withContext(Dispatchers.Default) {
            queries.update(
                nome = etapa.nome,
                orcamentoEtapa = etapa.orcamentoEtapa,
                dataInicio = etapa.dataInicio,
                dataFim = etapa.dataFim,
                progressoPercent = etapa.progressoPercent.toLong(),
                status = etapa.status.name,
                id = etapa.id,
            )
        }
    }

    override suspend fun reordenar(etapaId: String, novaOrdem: Int) {
        withContext(Dispatchers.Default) { queries.updateOrdem(novaOrdem.toLong(), etapaId) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarDoProjeto(projetoId: String): Flow<List<Etapa>> =
        queries.selectAtivasDoProjeto(projetoId).asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Etapa.toDomain() = Etapa(
    id = id,
    projetoId = projetoId,
    nome = nome,
    ordem = ordem.toInt(),
    orcamentoEtapa = orcamentoEtapa,
    dataInicio = dataInicio,
    dataFim = dataFim,
    progressoPercent = progressoPercent.toInt(),
    status = StatusEtapa.valueOf(status),
    ativo = ativo,
)
