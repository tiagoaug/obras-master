package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Equipe
import br.com.tiago.obramaster.server.db.EquipeMembros
import br.com.tiago.obramaster.server.db.Equipes
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object EquipeRepository {

    private fun membrosDe(equipeId: String): Set<String> =
        EquipeMembros.selectAll().andWhere { EquipeMembros.equipeId eq equipeId }.map { it[EquipeMembros.pessoaId] }.toSet()

    private fun rowToEquipe(row: ResultRow) = Equipe(
        id = row[Equipes.id],
        nome = row[Equipes.nome],
        liderPessoaId = row[Equipes.liderPessoaId],
        membrosIds = membrosDe(row[Equipes.id]),
        ativo = row[Equipes.ativo],
    )

    private fun substituirMembros(equipeId: String, membrosIds: Set<String>) {
        EquipeMembros.deleteWhere { EquipeMembros.equipeId eq equipeId }
        if (membrosIds.isNotEmpty()) {
            EquipeMembros.batchInsert(membrosIds) { pessoaId ->
                this[EquipeMembros.equipeId] = equipeId
                this[EquipeMembros.pessoaId] = pessoaId
            }
        }
    }

    fun listar(empresaId: String): List<Equipe> = transaction {
        Equipes.selectAll()
            .andWhere { Equipes.empresaId eq empresaId }
            .andWhere { Equipes.deletedAt.isNull() }
            .map(::rowToEquipe)
    }

    fun buscarPorId(empresaId: String, id: String): Equipe? = transaction {
        Equipes.selectAll()
            .andWhere { Equipes.id eq id }
            .andWhere { Equipes.empresaId eq empresaId }
            .andWhere { Equipes.deletedAt.isNull() }
            .map(::rowToEquipe)
            .singleOrNull()
    }

    fun criar(empresaId: String, equipe: Equipe): Equipe = transaction {
        Equipes.insert {
            it[id] = equipe.id
            it[Equipes.empresaId] = empresaId
            it[nome] = equipe.nome
            it[liderPessoaId] = equipe.liderPessoaId
            it[ativo] = equipe.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        substituirMembros(equipe.id, equipe.membrosIds)
        equipe
    }

    fun atualizar(empresaId: String, equipe: Equipe): Boolean = transaction {
        val linhas = Equipes.update({ (Equipes.id eq equipe.id) and (Equipes.empresaId eq empresaId) }) {
            it[nome] = equipe.nome
            it[liderPessoaId] = equipe.liderPessoaId
            it[ativo] = equipe.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        if (linhas > 0) substituirMembros(equipe.id, equipe.membrosIds)
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Equipes.update({ (Equipes.id eq id) and (Equipes.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
