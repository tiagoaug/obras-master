package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import br.com.tiago.obramaster.server.db.CentrosDeCusto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object CentroDeCustoRepository {

    private fun rowToCentro(row: ResultRow) = CentroDeCusto(
        id = row[CentrosDeCusto.id],
        nome = row[CentrosDeCusto.nome],
        tipo = TipoCentroDeCusto.valueOf(row[CentrosDeCusto.tipo]),
        projetoId = row[CentrosDeCusto.projetoId],
        ativo = row[CentrosDeCusto.ativo],
    )

    fun listar(empresaId: String): List<CentroDeCusto> = transaction {
        CentrosDeCusto.selectAll()
            .andWhere { CentrosDeCusto.empresaId eq empresaId }
            .andWhere { CentrosDeCusto.deletedAt.isNull() }
            .map(::rowToCentro)
    }

    fun buscarPorId(empresaId: String, id: String): CentroDeCusto? = transaction {
        CentrosDeCusto.selectAll()
            .andWhere { CentrosDeCusto.id eq id }
            .andWhere { CentrosDeCusto.empresaId eq empresaId }
            .andWhere { CentrosDeCusto.deletedAt.isNull() }
            .map(::rowToCentro)
            .singleOrNull()
    }

    fun criar(empresaId: String, centro: CentroDeCusto): CentroDeCusto = transaction {
        CentrosDeCusto.insert {
            it[id] = centro.id
            it[CentrosDeCusto.empresaId] = empresaId
            it[nome] = centro.nome
            it[tipo] = centro.tipo.name
            it[projetoId] = centro.projetoId
            it[ativo] = centro.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        centro
    }

    fun atualizar(empresaId: String, centro: CentroDeCusto): Boolean = transaction {
        val linhas = CentrosDeCusto.update({ (CentrosDeCusto.id eq centro.id) and (CentrosDeCusto.empresaId eq empresaId) }) {
            it[nome] = centro.nome
            it[tipo] = centro.tipo.name
            it[projetoId] = centro.projetoId
            it[ativo] = centro.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = CentrosDeCusto.update({ (CentrosDeCusto.id eq id) and (CentrosDeCusto.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
