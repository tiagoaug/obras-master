package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ColaboradorRepository {
    suspend fun listarAtivos(): List<Colaborador>
    suspend fun buscarPorId(id: String): Colaborador?
    suspend fun buscarPorLogin(login: String): Colaborador?
    suspend fun existeAlgumColaborador(): Boolean
    suspend fun salvar(colaborador: Colaborador)
    suspend fun atualizar(colaborador: Colaborador)
    suspend fun atualizarSenha(id: String, senhaHash: String, salt: String)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<Colaborador>>
}

class SqlDelightColaboradorRepository(
    private val db: ObraMasterDatabase,
) : ColaboradorRepository {
    private val queries = db.colaboradorQueries

    override suspend fun listarAtivos(): List<Colaborador> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun buscarPorId(id: String): Colaborador? = withContext(Dispatchers.Default) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun buscarPorLogin(login: String): Colaborador? = withContext(Dispatchers.Default) {
        queries.selectByLogin(login).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun existeAlgumColaborador(): Boolean = withContext(Dispatchers.Default) {
        queries.countAtivos().executeAsOne() > 0
    }

    override suspend fun salvar(colaborador: Colaborador) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = colaborador.id,
                nome = colaborador.nome,
                login = colaborador.login,
                senhaHash = colaborador.senhaHash,
                salt = colaborador.salt,
                ativo = colaborador.ativo,
                ehGestor = colaborador.ehGestor,
            )
        }
    }

    override suspend fun atualizar(colaborador: Colaborador) {
        withContext(Dispatchers.Default) {
            queries.update(
                nome = colaborador.nome,
                login = colaborador.login,
                ehGestor = colaborador.ehGestor,
                id = colaborador.id,
            )
        }
    }

    override suspend fun atualizarSenha(id: String, senhaHash: String, salt: String) {
        withContext(Dispatchers.Default) {
            queries.updateSenha(senhaHash = senhaHash, salt = salt, id = id)
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) {
            queries.softDelete(id)
        }
    }

    override fun observarAtivos(): Flow<List<Colaborador>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Colaborador.toDomain() = Colaborador(
    id = id,
    nome = nome,
    login = login,
    senhaHash = senhaHash,
    salt = salt,
    ativo = ativo,
    ehGestor = ehGestor,
)
