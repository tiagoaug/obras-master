package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Equipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface EquipeRepository {
    suspend fun listarAtivas(): List<Equipe>

    /** [membrosIds] substitui a lista inteira de membros da equipe. */
    suspend fun salvar(equipe: Equipe)
    suspend fun atualizar(equipe: Equipe)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Equipe>>
}

class SqlDelightEquipeRepository(
    private val db: ObraMasterDatabase,
) : EquipeRepository {
    private val queries = db.equipeQueries

    override suspend fun listarAtivas(): List<Equipe> = withContext(Dispatchers.Default) {
        val membrosPorEquipe = queries.selectTodosMembros().executeAsList().groupBy({ it.equipeId }, { it.pessoaId })
        queries.selectAtivas().executeAsList().map { it.toDomain(membrosPorEquipe[it.id].orEmpty().toSet()) }
    }

    override suspend fun salvar(equipe: Equipe) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insert(id = equipe.id, nome = equipe.nome, liderPessoaId = equipe.liderPessoaId, ativo = equipe.ativo)
                equipe.membrosIds.forEach { pessoaId -> queries.insertMembro(equipe.id, pessoaId) }
            }
        }
    }

    override suspend fun atualizar(equipe: Equipe) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.update(equipe.nome, equipe.liderPessoaId, equipe.id)
                queries.deleteMembrosDaEquipe(equipe.id)
                equipe.membrosIds.forEach { pessoaId -> queries.insertMembro(equipe.id, pessoaId) }
            }
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivas(): Flow<List<Equipe>> =
        combine(
            queries.selectAtivas().asFlow().mapToList(Dispatchers.Default),
            queries.selectTodosMembros().asFlow().mapToList(Dispatchers.Default),
        ) { equipes, membros ->
            val membrosPorEquipe = membros.groupBy({ it.equipeId }, { it.pessoaId })
            equipes.map { it.toDomain(membrosPorEquipe[it.id].orEmpty().toSet()) }
        }
}

private fun br.com.tiago.obramaster.db.Equipe.toDomain(membrosIds: Set<String>) = Equipe(
    id = id,
    nome = nome,
    liderPessoaId = liderPessoaId,
    membrosIds = membrosIds,
    ativo = ativo,
)
