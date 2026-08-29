package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusProjeto
import br.com.tiago.obramaster.server.db.Projetos
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object ProjetoRepository {

    private fun rowToProjeto(row: ResultRow) = Projeto(
        id = row[Projetos.id],
        nome = row[Projetos.nome],
        clienteId = row[Projetos.clienteId],
        endereco = row[Projetos.endereco],
        areaConstruidaM2 = row[Projetos.areaConstruidaM2],
        areaTerrenoM2 = row[Projetos.areaTerrenoM2],
        orcamentoTotal = row[Projetos.orcamentoTotal],
        dataInicio = row[Projetos.dataInicio],
        dataPrevisaoFim = row[Projetos.dataPrevisaoFim],
        status = StatusProjeto.valueOf(row[Projetos.status]),
        fotoCapaUri = row[Projetos.fotoCapaUri],
        ativo = row[Projetos.ativo],
    )

    fun listar(empresaId: String): List<Projeto> = transaction {
        Projetos.selectAll()
            .andWhere { Projetos.empresaId eq empresaId }
            .andWhere { Projetos.deletedAt.isNull() }
            .map(::rowToProjeto)
    }

    fun buscarPorId(empresaId: String, id: String): Projeto? = transaction {
        Projetos.selectAll()
            .andWhere { Projetos.id eq id }
            .andWhere { Projetos.empresaId eq empresaId }
            .andWhere { Projetos.deletedAt.isNull() }
            .map(::rowToProjeto)
            .singleOrNull()
    }

    fun criar(empresaId: String, projeto: Projeto): Projeto = transaction {
        val agora = System.currentTimeMillis()
        Projetos.insert {
            it[id] = projeto.id
            it[Projetos.empresaId] = empresaId
            it[nome] = projeto.nome
            it[clienteId] = projeto.clienteId
            it[endereco] = projeto.endereco
            it[areaConstruidaM2] = projeto.areaConstruidaM2
            it[areaTerrenoM2] = projeto.areaTerrenoM2
            it[orcamentoTotal] = projeto.orcamentoTotal
            it[dataInicio] = projeto.dataInicio
            it[dataPrevisaoFim] = projeto.dataPrevisaoFim
            it[status] = projeto.status.name
            it[fotoCapaUri] = projeto.fotoCapaUri
            it[ativo] = projeto.ativo
            it[updatedAt] = agora
        }
        projeto
    }

    fun atualizar(empresaId: String, projeto: Projeto): Boolean = transaction {
        val linhas = Projetos.update({ (Projetos.id eq projeto.id) and (Projetos.empresaId eq empresaId) }) {
            it[nome] = projeto.nome
            it[clienteId] = projeto.clienteId
            it[endereco] = projeto.endereco
            it[areaConstruidaM2] = projeto.areaConstruidaM2
            it[areaTerrenoM2] = projeto.areaTerrenoM2
            it[orcamentoTotal] = projeto.orcamentoTotal
            it[dataInicio] = projeto.dataInicio
            it[dataPrevisaoFim] = projeto.dataPrevisaoFim
            it[status] = projeto.status.name
            it[fotoCapaUri] = projeto.fotoCapaUri
            it[ativo] = projeto.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Projetos.update({ (Projetos.id eq id) and (Projetos.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
