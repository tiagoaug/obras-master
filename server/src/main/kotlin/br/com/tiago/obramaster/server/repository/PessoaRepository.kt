package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.server.db.Pessoas
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PessoaRepository {

    private fun tagsParaTexto(tags: Set<TagPessoa>) = tags.joinToString(",") { it.name }
    private fun textoParaTags(texto: String) = texto.split(",").filter { it.isNotBlank() }.map { TagPessoa.valueOf(it) }.toSet()

    private fun rowToPessoa(row: ResultRow) = Pessoa(
        id = row[Pessoas.id],
        nome = row[Pessoas.nome],
        tags = textoParaTags(row[Pessoas.tags]),
        telefone = row[Pessoas.telefone],
        email = row[Pessoas.email],
        endereco = row[Pessoas.endereco],
        documento = row[Pessoas.documento],
        fotoUri = row[Pessoas.fotoUri],
        observacoes = row[Pessoas.observacoes],
        ativo = row[Pessoas.ativo],
    )

    fun listar(empresaId: String): List<Pessoa> = transaction {
        Pessoas.selectAll()
            .andWhere { Pessoas.empresaId eq empresaId }
            .andWhere { Pessoas.deletedAt.isNull() }
            .map(::rowToPessoa)
    }

    fun buscarPorId(empresaId: String, id: String): Pessoa? = transaction {
        Pessoas.selectAll()
            .andWhere { Pessoas.id eq id }
            .andWhere { Pessoas.empresaId eq empresaId }
            .andWhere { Pessoas.deletedAt.isNull() }
            .map(::rowToPessoa)
            .singleOrNull()
    }

    fun criar(empresaId: String, pessoa: Pessoa): Pessoa = transaction {
        Pessoas.insert {
            it[id] = pessoa.id
            it[Pessoas.empresaId] = empresaId
            it[nome] = pessoa.nome
            it[tags] = tagsParaTexto(pessoa.tags)
            it[telefone] = pessoa.telefone
            it[email] = pessoa.email
            it[endereco] = pessoa.endereco
            it[documento] = pessoa.documento
            it[fotoUri] = pessoa.fotoUri
            it[observacoes] = pessoa.observacoes
            it[ativo] = pessoa.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        pessoa
    }

    fun atualizar(empresaId: String, pessoa: Pessoa): Boolean = transaction {
        val linhas = Pessoas.update({ (Pessoas.id eq pessoa.id) and (Pessoas.empresaId eq empresaId) }) {
            it[nome] = pessoa.nome
            it[tags] = tagsParaTexto(pessoa.tags)
            it[telefone] = pessoa.telefone
            it[email] = pessoa.email
            it[endereco] = pessoa.endereco
            it[documento] = pessoa.documento
            it[fotoUri] = pessoa.fotoUri
            it[observacoes] = pessoa.observacoes
            it[ativo] = pessoa.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Pessoas.update({ (Pessoas.id eq id) and (Pessoas.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
