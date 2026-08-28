package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Funcionario
import br.com.tiago.obramaster.domain.TipoContratacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface FuncionarioRepository {
    suspend fun listarAtivos(): List<Funcionario>
    suspend fun buscarPorPessoaId(pessoaId: String): Funcionario?
    suspend fun salvar(funcionario: Funcionario)
    suspend fun atualizar(funcionario: Funcionario)
    suspend fun desativar(pessoaId: String)
    fun observarAtivos(): Flow<List<Funcionario>>
}

class SqlDelightFuncionarioRepository(
    private val db: ObraMasterDatabase,
) : FuncionarioRepository {
    private val queries = db.funcionarioQueries

    override suspend fun listarAtivos(): List<Funcionario> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun buscarPorPessoaId(pessoaId: String): Funcionario? = withContext(Dispatchers.Default) {
        queries.selectPorPessoaId(pessoaId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun salvar(funcionario: Funcionario) {
        withContext(Dispatchers.Default) {
            queries.insert(
                pessoaId = funcionario.pessoaId,
                funcao = funcionario.funcao,
                tipoContratacao = funcionario.tipoContratacao.name,
                valorBase = funcionario.valorBase,
                ativo = funcionario.ativo,
            )
        }
    }

    override suspend fun atualizar(funcionario: Funcionario) {
        withContext(Dispatchers.Default) {
            queries.update(funcionario.funcao, funcionario.tipoContratacao.name, funcionario.valorBase, funcionario.pessoaId)
        }
    }

    override suspend fun desativar(pessoaId: String) {
        withContext(Dispatchers.Default) { queries.softDelete(pessoaId) }
    }

    override fun observarAtivos(): Flow<List<Funcionario>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Funcionario.toDomain() = Funcionario(
    pessoaId = pessoaId,
    funcao = funcao,
    tipoContratacao = TipoContratacao.valueOf(tipoContratacao),
    valorBase = valorBase,
    ativo = ativo,
)
