package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.StatusEtapa
import br.com.tiago.obramaster.server.db.Etapas
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object EtapaRepository {

    private fun rowToEtapa(row: ResultRow) = Etapa(
        id = row[Etapas.id],
        projetoId = row[Etapas.projetoId],
        nome = row[Etapas.nome],
        ordem = row[Etapas.ordem],
        orcamentoEtapa = row[Etapas.orcamentoEtapa],
        dataInicio = row[Etapas.dataInicio],
        dataFim = row[Etapas.dataFim],
        dataInicioReal = row[Etapas.dataInicioReal],
        dataFimReal = row[Etapas.dataFimReal],
        progressoPercent = row[Etapas.progressoPercent],
        status = StatusEtapa.valueOf(row[Etapas.status]),
        ativo = row[Etapas.ativo],
    )

    fun listarPorProjeto(empresaId: String, projetoId: String): List<Etapa> = transaction {
        Etapas.selectAll()
            .andWhere { Etapas.projetoId eq projetoId }
            .andWhere { Etapas.empresaId eq empresaId }
            .andWhere { Etapas.deletedAt.isNull() }
            .map(::rowToEtapa)
    }

    fun buscarPorId(empresaId: String, id: String): Etapa? = transaction {
        Etapas.selectAll()
            .andWhere { Etapas.id eq id }
            .andWhere { Etapas.empresaId eq empresaId }
            .andWhere { Etapas.deletedAt.isNull() }
            .map(::rowToEtapa)
            .singleOrNull()
    }

    fun criar(empresaId: String, etapa: Etapa): Etapa = transaction {
        Etapas.insert {
            it[id] = etapa.id
            it[Etapas.empresaId] = empresaId
            it[projetoId] = etapa.projetoId
            it[nome] = etapa.nome
            it[ordem] = etapa.ordem
            it[orcamentoEtapa] = etapa.orcamentoEtapa
            it[dataInicio] = etapa.dataInicio
            it[dataFim] = etapa.dataFim
            it[dataInicioReal] = etapa.dataInicioReal
            it[dataFimReal] = etapa.dataFimReal
            it[progressoPercent] = etapa.progressoPercent
            it[status] = etapa.status.name
            it[ativo] = etapa.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        etapa
    }

    fun atualizar(empresaId: String, etapa: Etapa): Boolean = transaction {
        val linhas = Etapas.update({ (Etapas.id eq etapa.id) and (Etapas.empresaId eq empresaId) }) {
            it[nome] = etapa.nome
            it[ordem] = etapa.ordem
            it[orcamentoEtapa] = etapa.orcamentoEtapa
            it[dataInicio] = etapa.dataInicio
            it[dataFim] = etapa.dataFim
            it[dataInicioReal] = etapa.dataInicioReal
            it[dataFimReal] = etapa.dataFimReal
            it[progressoPercent] = etapa.progressoPercent
            it[status] = etapa.status.name
            it[ativo] = etapa.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Etapas.update({ (Etapas.id eq id) and (Etapas.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
