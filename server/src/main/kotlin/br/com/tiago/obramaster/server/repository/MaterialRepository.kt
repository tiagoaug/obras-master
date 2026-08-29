package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.server.db.Materiais
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object MaterialRepository {

    private fun rowToMaterial(row: ResultRow) = Material(
        id = row[Materiais.id],
        nome = row[Materiais.nome],
        unidadePadrao = row[Materiais.unidadePadrao],
        precoReferencia = row[Materiais.precoReferencia],
        categoria = row[Materiais.categoria],
        corId = row[Materiais.corId],
        ativo = row[Materiais.ativo],
    )

    fun listar(empresaId: String): List<Material> = transaction {
        Materiais.selectAll().andWhere { Materiais.empresaId eq empresaId }.andWhere { Materiais.deletedAt.isNull() }.map(::rowToMaterial)
    }

    fun buscarPorId(empresaId: String, id: String): Material? = transaction {
        Materiais.selectAll().andWhere { Materiais.id eq id }.andWhere { Materiais.empresaId eq empresaId }.andWhere { Materiais.deletedAt.isNull() }
            .map(::rowToMaterial).singleOrNull()
    }

    fun criar(empresaId: String, material: Material): Material = transaction {
        Materiais.insert {
            it[id] = material.id
            it[Materiais.empresaId] = empresaId
            it[nome] = material.nome
            it[unidadePadrao] = material.unidadePadrao
            it[precoReferencia] = material.precoReferencia
            it[categoria] = material.categoria
            it[corId] = material.corId
            it[ativo] = material.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        material
    }

    fun atualizar(empresaId: String, material: Material): Boolean = transaction {
        val linhas = Materiais.update({ (Materiais.id eq material.id) and (Materiais.empresaId eq empresaId) }) {
            it[nome] = material.nome
            it[unidadePadrao] = material.unidadePadrao
            it[precoReferencia] = material.precoReferencia
            it[categoria] = material.categoria
            it[corId] = material.corId
            it[ativo] = material.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Materiais.update({ (Materiais.id eq id) and (Materiais.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
