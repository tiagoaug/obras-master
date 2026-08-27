package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PessoaRepository {
    suspend fun listarAtivas(): List<Pessoa>
    suspend fun salvar(pessoa: Pessoa)
    suspend fun atualizar(pessoa: Pessoa)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Pessoa>>
}

class SqlDelightPessoaRepository(
    private val db: ObraMasterDatabase,
) : PessoaRepository {
    private val queries = db.pessoaQueries

    override suspend fun listarAtivas(): List<Pessoa> = withContext(Dispatchers.Default) {
        val tagsPorPessoa = queries.selectTodasTags().executeAsList().groupBy({ it.pessoaId }, { it.tag })
        queries.selectAtivas().executeAsList().map { it.toDomain(tagsPorPessoa[it.id].orEmpty()) }
    }

    override suspend fun salvar(pessoa: Pessoa) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insertPessoa(
                    id = pessoa.id,
                    nome = pessoa.nome,
                    telefone = pessoa.telefone,
                    email = pessoa.email,
                    endereco = pessoa.endereco,
                    documento = pessoa.documento,
                    fotoUri = pessoa.fotoUri,
                    observacoes = pessoa.observacoes,
                    ativo = pessoa.ativo,
                )
                pessoa.tags.forEach { queries.insertTag(pessoa.id, it.name) }
            }
        }
    }

    override suspend fun atualizar(pessoa: Pessoa) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.updatePessoa(
                    nome = pessoa.nome,
                    telefone = pessoa.telefone,
                    email = pessoa.email,
                    endereco = pessoa.endereco,
                    documento = pessoa.documento,
                    fotoUri = pessoa.fotoUri,
                    observacoes = pessoa.observacoes,
                    id = pessoa.id,
                )
                queries.deleteTagsDe(pessoa.id)
                pessoa.tags.forEach { queries.insertTag(pessoa.id, it.name) }
            }
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDeletePessoa(id) }
    }

    override fun observarAtivas(): Flow<List<Pessoa>> =
        combine(
            queries.selectAtivas().asFlow().mapToList(Dispatchers.Default),
            queries.selectTodasTags().asFlow().mapToList(Dispatchers.Default),
        ) { pessoas, tags ->
            val tagsPorPessoa = tags.groupBy({ it.pessoaId }, { it.tag })
            pessoas.map { it.toDomain(tagsPorPessoa[it.id].orEmpty()) }
        }
}

private fun br.com.tiago.obramaster.db.Pessoa.toDomain(tags: List<String>) = Pessoa(
    id = id,
    nome = nome,
    tags = tags.mapNotNull { nome -> runCatching { TagPessoa.valueOf(nome) }.getOrNull() }.toSet(),
    telefone = telefone,
    email = email,
    endereco = endereco,
    documento = documento,
    fotoUri = fotoUri,
    observacoes = observacoes,
    ativo = ativo,
)
