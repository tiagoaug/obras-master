package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Tarefa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface TarefaRepository {
    suspend fun listarDaEtapa(etapaId: String): List<Tarefa>
    suspend fun salvar(tarefa: Tarefa)
    suspend fun atualizar(tarefa: Tarefa)
    suspend fun excluir(id: String)
    fun observarDaEtapa(etapaId: String): Flow<List<Tarefa>>
}

class SqlDelightTarefaRepository(
    private val db: ObraMasterDatabase,
) : TarefaRepository {
    private val queries = db.tarefaQueries

    override suspend fun listarDaEtapa(etapaId: String): List<Tarefa> = withContext(Dispatchers.Default) {
        queries.selectDaEtapa(etapaId).executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(tarefa: Tarefa) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = tarefa.id,
                etapaId = tarefa.etapaId,
                descricao = tarefa.descricao,
                responsavelPessoaId = tarefa.responsavelPessoaId,
                prazo = tarefa.prazo,
                concluida = tarefa.concluida,
            )
        }
    }

    override suspend fun atualizar(tarefa: Tarefa) {
        withContext(Dispatchers.Default) {
            queries.update(
                descricao = tarefa.descricao,
                responsavelPessoaId = tarefa.responsavelPessoaId,
                prazo = tarefa.prazo,
                concluida = tarefa.concluida,
                id = tarefa.id,
            )
        }
    }

    override suspend fun excluir(id: String) {
        withContext(Dispatchers.Default) { queries.excluir(id) }
    }

    override fun observarDaEtapa(etapaId: String): Flow<List<Tarefa>> =
        queries.selectDaEtapa(etapaId).asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Tarefa.toDomain() = Tarefa(
    id = id,
    etapaId = etapaId,
    descricao = descricao,
    responsavelPessoaId = responsavelPessoaId,
    prazo = prazo,
    concluida = concluida,
)
