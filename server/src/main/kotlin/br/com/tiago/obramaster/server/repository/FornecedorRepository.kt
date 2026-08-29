package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Fornecedor
import br.com.tiago.obramaster.server.db.Fornecedores
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/** Chave é `pessoaId`, mesmo padrão de FuncionarioRepository. */
object FornecedorRepository {

    private fun rowToFornecedor(row: ResultRow) = Fornecedor(
        pessoaId = row[Fornecedores.pessoaId],
        cnpjCpf = row[Fornecedores.cnpjCpf],
        observacoes = row[Fornecedores.observacoes],
        ativo = row[Fornecedores.ativo],
    )

    fun listar(empresaId: String): List<Fornecedor> = transaction {
        Fornecedores.selectAll().andWhere { Fornecedores.empresaId eq empresaId }.andWhere { Fornecedores.deletedAt.isNull() }.map(::rowToFornecedor)
    }

    fun buscarPorPessoaId(empresaId: String, pessoaId: String): Fornecedor? = transaction {
        Fornecedores.selectAll().andWhere { Fornecedores.pessoaId eq pessoaId }.andWhere { Fornecedores.empresaId eq empresaId }.andWhere { Fornecedores.deletedAt.isNull() }
            .map(::rowToFornecedor).singleOrNull()
    }

    fun criar(empresaId: String, fornecedor: Fornecedor): Fornecedor = transaction {
        Fornecedores.insert {
            it[pessoaId] = fornecedor.pessoaId
            it[Fornecedores.empresaId] = empresaId
            it[cnpjCpf] = fornecedor.cnpjCpf
            it[observacoes] = fornecedor.observacoes
            it[ativo] = fornecedor.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        fornecedor
    }

    fun atualizar(empresaId: String, fornecedor: Fornecedor): Boolean = transaction {
        val linhas = Fornecedores.update({ (Fornecedores.pessoaId eq fornecedor.pessoaId) and (Fornecedores.empresaId eq empresaId) }) {
            it[cnpjCpf] = fornecedor.cnpjCpf
            it[observacoes] = fornecedor.observacoes
            it[ativo] = fornecedor.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, pessoaId: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Fornecedores.update({ (Fornecedores.pessoaId eq pessoaId) and (Fornecedores.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
