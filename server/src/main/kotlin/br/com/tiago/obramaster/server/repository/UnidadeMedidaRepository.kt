package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.UnidadeMedida
import br.com.tiago.obramaster.server.db.UnidadesMedida
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object UnidadeMedidaRepository {

    private fun rowToUnidade(row: ResultRow) = UnidadeMedida(
        id = row[UnidadesMedida.id],
        sigla = row[UnidadesMedida.sigla],
        nome = row[UnidadesMedida.nome],
        ativo = row[UnidadesMedida.ativo],
    )

    fun listar(empresaId: String): List<UnidadeMedida> = transaction {
        UnidadesMedida.selectAll().andWhere { UnidadesMedida.empresaId eq empresaId }.andWhere { UnidadesMedida.deletedAt.isNull() }.map(::rowToUnidade)
    }

    fun buscarPorId(empresaId: String, id: String): UnidadeMedida? = transaction {
        UnidadesMedida.selectAll().andWhere { UnidadesMedida.id eq id }.andWhere { UnidadesMedida.empresaId eq empresaId }.andWhere { UnidadesMedida.deletedAt.isNull() }
            .map(::rowToUnidade).singleOrNull()
    }

    fun criar(empresaId: String, unidade: UnidadeMedida): UnidadeMedida = transaction {
        UnidadesMedida.insert {
            it[id] = unidade.id
            it[UnidadesMedida.empresaId] = empresaId
            it[sigla] = unidade.sigla
            it[nome] = unidade.nome
            it[ativo] = unidade.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        unidade
    }

    fun atualizar(empresaId: String, unidade: UnidadeMedida): Boolean = transaction {
        val linhas = UnidadesMedida.update({ (UnidadesMedida.id eq unidade.id) and (UnidadesMedida.empresaId eq empresaId) }) {
            it[sigla] = unidade.sigla
            it[nome] = unidade.nome
            it[ativo] = unidade.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = UnidadesMedida.update({ (UnidadesMedida.id eq id) and (UnidadesMedida.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
