package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Fornecedor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface FornecedorRepository {
    suspend fun listarAtivos(): List<Fornecedor>
    suspend fun salvar(fornecedor: Fornecedor)
    suspend fun atualizar(fornecedor: Fornecedor)
    suspend fun desativar(pessoaId: String)
    fun observarAtivos(): Flow<List<Fornecedor>>
}

class SqlDelightFornecedorRepository(
    private val db: ObraMasterDatabase,
) : FornecedorRepository {
    private val queries = db.fornecedorQueries

    override suspend fun listarAtivos(): List<Fornecedor> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(fornecedor: Fornecedor) {
        withContext(Dispatchers.Default) {
            queries.insert(fornecedor.pessoaId, fornecedor.cnpjCpf, fornecedor.observacoes, fornecedor.ativo)
        }
    }

    override suspend fun atualizar(fornecedor: Fornecedor) {
        withContext(Dispatchers.Default) { queries.update(fornecedor.cnpjCpf, fornecedor.observacoes, fornecedor.pessoaId) }
    }

    override suspend fun desativar(pessoaId: String) {
        withContext(Dispatchers.Default) { queries.softDelete(pessoaId) }
    }

    override fun observarAtivos(): Flow<List<Fornecedor>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Fornecedor.toDomain() = Fornecedor(
    pessoaId = pessoaId,
    cnpjCpf = cnpjCpf,
    observacoes = observacoes,
    ativo = ativo,
)
