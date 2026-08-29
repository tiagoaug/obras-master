package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Funcionario
import br.com.tiago.obramaster.domain.TipoContratacao
import br.com.tiago.obramaster.server.db.Funcionarios
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/** Chave é `pessoaId`, não um `id` próprio (ver Funcionario no :core) — por isso `criar` faz
 * upsert-like (insere; se já existir pra essa pessoa, o `PUT` é quem atualiza). */
object FuncionarioRepository {

    private fun rowToFuncionario(row: ResultRow) = Funcionario(
        pessoaId = row[Funcionarios.pessoaId],
        funcao = row[Funcionarios.funcao],
        tipoContratacao = TipoContratacao.valueOf(row[Funcionarios.tipoContratacao]),
        valorBase = row[Funcionarios.valorBase],
        ativo = row[Funcionarios.ativo],
    )

    fun listar(empresaId: String): List<Funcionario> = transaction {
        Funcionarios.selectAll()
            .andWhere { Funcionarios.empresaId eq empresaId }
            .andWhere { Funcionarios.deletedAt.isNull() }
            .map(::rowToFuncionario)
    }

    fun buscarPorPessoaId(empresaId: String, pessoaId: String): Funcionario? = transaction {
        Funcionarios.selectAll()
            .andWhere { Funcionarios.pessoaId eq pessoaId }
            .andWhere { Funcionarios.empresaId eq empresaId }
            .andWhere { Funcionarios.deletedAt.isNull() }
            .map(::rowToFuncionario)
            .singleOrNull()
    }

    fun criar(empresaId: String, funcionario: Funcionario): Funcionario = transaction {
        Funcionarios.insert {
            it[pessoaId] = funcionario.pessoaId
            it[Funcionarios.empresaId] = empresaId
            it[funcao] = funcionario.funcao
            it[tipoContratacao] = funcionario.tipoContratacao.name
            it[valorBase] = funcionario.valorBase
            it[ativo] = funcionario.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        funcionario
    }

    fun atualizar(empresaId: String, funcionario: Funcionario): Boolean = transaction {
        val linhas = Funcionarios.update({ (Funcionarios.pessoaId eq funcionario.pessoaId) and (Funcionarios.empresaId eq empresaId) }) {
            it[funcao] = funcionario.funcao
            it[tipoContratacao] = funcionario.tipoContratacao.name
            it[valorBase] = funcionario.valorBase
            it[ativo] = funcionario.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, pessoaId: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Funcionarios.update({ (Funcionarios.pessoaId eq pessoaId) and (Funcionarios.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
