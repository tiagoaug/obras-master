package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Cor
import br.com.tiago.obramaster.server.db.Cores
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object CorRepository {

    private fun rowToCor(row: ResultRow) = Cor(
        id = row[Cores.id],
        nome = row[Cores.nome],
        hex = row[Cores.hex],
        codigoFabricante = row[Cores.codigoFabricante],
        ativo = row[Cores.ativo],
    )

    fun listar(empresaId: String): List<Cor> = transaction {
        Cores.selectAll().andWhere { Cores.empresaId eq empresaId }.andWhere { Cores.deletedAt.isNull() }.map(::rowToCor)
    }

    fun buscarPorId(empresaId: String, id: String): Cor? = transaction {
        Cores.selectAll().andWhere { Cores.id eq id }.andWhere { Cores.empresaId eq empresaId }.andWhere { Cores.deletedAt.isNull() }
            .map(::rowToCor).singleOrNull()
    }

    fun criar(empresaId: String, cor: Cor): Cor = transaction {
        Cores.insert {
            it[id] = cor.id
            it[Cores.empresaId] = empresaId
            it[nome] = cor.nome
            it[hex] = cor.hex
            it[codigoFabricante] = cor.codigoFabricante
            it[ativo] = cor.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        cor
    }

    fun atualizar(empresaId: String, cor: Cor): Boolean = transaction {
        val linhas = Cores.update({ (Cores.id eq cor.id) and (Cores.empresaId eq empresaId) }) {
            it[nome] = cor.nome
            it[hex] = cor.hex
            it[codigoFabricante] = cor.codigoFabricante
            it[ativo] = cor.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Cores.update({ (Cores.id eq id) and (Cores.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
