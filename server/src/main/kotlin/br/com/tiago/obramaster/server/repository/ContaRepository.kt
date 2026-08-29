package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.TipoConta
import br.com.tiago.obramaster.server.db.Contas
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object ContaRepository {

    private fun rowToConta(row: ResultRow) = Conta(
        id = row[Contas.id],
        nome = row[Contas.nome],
        tipo = TipoConta.valueOf(row[Contas.tipo]),
        banco = row[Contas.banco],
        agencia = row[Contas.agencia],
        numeroConta = row[Contas.numeroConta],
        saldoInicial = row[Contas.saldoInicial],
        dataSaldoInicial = row[Contas.dataSaldoInicial],
        ativo = row[Contas.ativo],
        cor = row[Contas.cor],
    )

    fun listar(empresaId: String): List<Conta> = transaction {
        Contas.selectAll()
            .andWhere { Contas.empresaId eq empresaId }
            .andWhere { Contas.deletedAt.isNull() }
            .map(::rowToConta)
    }

    fun buscarPorId(empresaId: String, id: String): Conta? = transaction {
        Contas.selectAll()
            .andWhere { Contas.id eq id }
            .andWhere { Contas.empresaId eq empresaId }
            .andWhere { Contas.deletedAt.isNull() }
            .map(::rowToConta)
            .singleOrNull()
    }

    fun criar(empresaId: String, conta: Conta): Conta = transaction {
        Contas.insert {
            it[id] = conta.id
            it[Contas.empresaId] = empresaId
            it[nome] = conta.nome
            it[tipo] = conta.tipo.name
            it[banco] = conta.banco
            it[agencia] = conta.agencia
            it[numeroConta] = conta.numeroConta
            it[saldoInicial] = conta.saldoInicial
            it[dataSaldoInicial] = conta.dataSaldoInicial
            it[ativo] = conta.ativo
            it[cor] = conta.cor
            it[updatedAt] = System.currentTimeMillis()
        }
        conta
    }

    fun atualizar(empresaId: String, conta: Conta): Boolean = transaction {
        val linhas = Contas.update({ (Contas.id eq conta.id) and (Contas.empresaId eq empresaId) }) {
            it[nome] = conta.nome
            it[tipo] = conta.tipo.name
            it[banco] = conta.banco
            it[agencia] = conta.agencia
            it[numeroConta] = conta.numeroConta
            it[saldoInicial] = conta.saldoInicial
            it[dataSaldoInicial] = conta.dataSaldoInicial
            it[ativo] = conta.ativo
            it[cor] = conta.cor
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Contas.update({ (Contas.id eq id) and (Contas.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
