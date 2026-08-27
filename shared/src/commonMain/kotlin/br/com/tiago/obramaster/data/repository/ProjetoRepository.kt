package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusProjeto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ProjetoRepository {
    suspend fun listarAtivos(): List<Projeto>
    suspend fun buscarPorId(id: String): Projeto?
    suspend fun salvar(projeto: Projeto)
    suspend fun atualizar(projeto: Projeto)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<Projeto>>
}

class SqlDelightProjetoRepository(
    private val db: ObraMasterDatabase,
) : ProjetoRepository {
    private val queries = db.projetoQueries

    override suspend fun listarAtivos(): List<Projeto> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun buscarPorId(id: String): Projeto? = withContext(Dispatchers.Default) {
        queries.selectPorId(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun salvar(projeto: Projeto) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = projeto.id,
                nome = projeto.nome,
                clienteId = projeto.clienteId,
                endereco = projeto.endereco,
                areaConstruidaM2 = projeto.areaConstruidaM2,
                areaTerrenoM2 = projeto.areaTerrenoM2,
                orcamentoTotal = projeto.orcamentoTotal,
                dataInicio = projeto.dataInicio,
                dataPrevisaoFim = projeto.dataPrevisaoFim,
                status = projeto.status.name,
                fotoCapaUri = projeto.fotoCapaUri,
                ativo = projeto.ativo,
            )
        }
    }

    override suspend fun atualizar(projeto: Projeto) {
        withContext(Dispatchers.Default) {
            queries.update(
                nome = projeto.nome,
                clienteId = projeto.clienteId,
                endereco = projeto.endereco,
                areaConstruidaM2 = projeto.areaConstruidaM2,
                areaTerrenoM2 = projeto.areaTerrenoM2,
                orcamentoTotal = projeto.orcamentoTotal,
                dataInicio = projeto.dataInicio,
                dataPrevisaoFim = projeto.dataPrevisaoFim,
                status = projeto.status.name,
                fotoCapaUri = projeto.fotoCapaUri,
                id = projeto.id,
            )
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivos(): Flow<List<Projeto>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Projeto.toDomain() = Projeto(
    id = id,
    nome = nome,
    clienteId = clienteId,
    endereco = endereco,
    areaConstruidaM2 = areaConstruidaM2,
    areaTerrenoM2 = areaTerrenoM2,
    orcamentoTotal = orcamentoTotal,
    dataInicio = dataInicio,
    dataPrevisaoFim = dataPrevisaoFim,
    status = StatusProjeto.valueOf(status),
    fotoCapaUri = fotoCapaUri,
    ativo = ativo,
)
