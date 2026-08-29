package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.TipoRegistroTrabalho
import br.com.tiago.obramaster.server.db.RegistrosTrabalho
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object RegistroTrabalhoRepository {

    private fun rowToRegistro(row: ResultRow) = RegistroTrabalho(
        id = row[RegistrosTrabalho.id],
        pessoaId = row[RegistrosTrabalho.pessoaId],
        projetoId = row[RegistrosTrabalho.projetoId],
        etapaId = row[RegistrosTrabalho.etapaId],
        data = row[RegistrosTrabalho.data],
        tipo = TipoRegistroTrabalho.valueOf(row[RegistrosTrabalho.tipo]),
        valor = row[RegistrosTrabalho.valor],
        observacao = row[RegistrosTrabalho.observacao],
        pago = row[RegistrosTrabalho.pago],
        pagamentoId = row[RegistrosTrabalho.pagamentoId],
        ativo = row[RegistrosTrabalho.ativo],
    )

    fun listar(empresaId: String, pessoaId: String? = null, pago: Boolean? = null): List<RegistroTrabalho> = transaction {
        RegistrosTrabalho.selectAll()
            .andWhere { RegistrosTrabalho.empresaId eq empresaId }
            .andWhere { RegistrosTrabalho.deletedAt.isNull() }
            .let { query -> if (pessoaId != null) query.andWhere { RegistrosTrabalho.pessoaId eq pessoaId } else query }
            .let { query -> if (pago != null) query.andWhere { RegistrosTrabalho.pago eq pago } else query }
            .map(::rowToRegistro)
    }

    fun buscarPorId(empresaId: String, id: String): RegistroTrabalho? = transaction {
        RegistrosTrabalho.selectAll()
            .andWhere { RegistrosTrabalho.id eq id }
            .andWhere { RegistrosTrabalho.empresaId eq empresaId }
            .andWhere { RegistrosTrabalho.deletedAt.isNull() }
            .map(::rowToRegistro)
            .singleOrNull()
    }

    fun criar(empresaId: String, registro: RegistroTrabalho): RegistroTrabalho = transaction {
        RegistrosTrabalho.insert {
            it[id] = registro.id
            it[RegistrosTrabalho.empresaId] = empresaId
            it[pessoaId] = registro.pessoaId
            it[projetoId] = registro.projetoId
            it[etapaId] = registro.etapaId
            it[data] = registro.data
            it[tipo] = registro.tipo.name
            it[valor] = registro.valor
            it[observacao] = registro.observacao
            it[pago] = registro.pago
            it[pagamentoId] = registro.pagamentoId
            it[ativo] = registro.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        registro
    }

    fun atualizar(empresaId: String, registro: RegistroTrabalho): Boolean = transaction {
        val linhas = RegistrosTrabalho.update({ (RegistrosTrabalho.id eq registro.id) and (RegistrosTrabalho.empresaId eq empresaId) }) {
            it[etapaId] = registro.etapaId
            it[data] = registro.data
            it[tipo] = registro.tipo.name
            it[valor] = registro.valor
            it[observacao] = registro.observacao
            it[pago] = registro.pago
            it[pagamentoId] = registro.pagamentoId
            it[ativo] = registro.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = RegistrosTrabalho.update({ (RegistrosTrabalho.id eq id) and (RegistrosTrabalho.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
