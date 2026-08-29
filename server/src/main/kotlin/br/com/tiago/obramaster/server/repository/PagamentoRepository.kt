package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.Pagamento
import br.com.tiago.obramaster.domain.StatusPagamento
import br.com.tiago.obramaster.server.db.Pagamentos
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PagamentoRepository {

    private fun rowToPagamento(row: ResultRow) = Pagamento(
        id = row[Pagamentos.id],
        pessoaId = row[Pagamentos.pessoaId],
        projetoId = row[Pagamentos.projetoId],
        periodo = row[Pagamentos.periodo],
        valorTotal = row[Pagamentos.valorTotal],
        dataPagamento = row[Pagamentos.dataPagamento],
        status = StatusPagamento.valueOf(row[Pagamentos.status]),
        comprovanteUri = row[Pagamentos.comprovanteUri],
        lancamentoFinanceiroId = row[Pagamentos.lancamentoFinanceiroId],
    )

    fun listar(empresaId: String, pessoaId: String? = null): List<Pagamento> = transaction {
        Pagamentos.selectAll()
            .andWhere { Pagamentos.empresaId eq empresaId }
            .andWhere { Pagamentos.deletedAt.isNull() }
            .let { query -> if (pessoaId != null) query.andWhere { Pagamentos.pessoaId eq pessoaId } else query }
            .map(::rowToPagamento)
    }

    fun buscarPorId(empresaId: String, id: String): Pagamento? = transaction {
        Pagamentos.selectAll()
            .andWhere { Pagamentos.id eq id }
            .andWhere { Pagamentos.empresaId eq empresaId }
            .andWhere { Pagamentos.deletedAt.isNull() }
            .map(::rowToPagamento)
            .singleOrNull()
    }

    fun criar(empresaId: String, pagamento: Pagamento): Pagamento = transaction {
        Pagamentos.insert {
            it[id] = pagamento.id
            it[Pagamentos.empresaId] = empresaId
            it[pessoaId] = pagamento.pessoaId
            it[projetoId] = pagamento.projetoId
            it[periodo] = pagamento.periodo
            it[valorTotal] = pagamento.valorTotal
            it[dataPagamento] = pagamento.dataPagamento
            it[status] = pagamento.status.name
            it[comprovanteUri] = pagamento.comprovanteUri
            it[lancamentoFinanceiroId] = pagamento.lancamentoFinanceiroId
            it[ativo] = true
            it[updatedAt] = System.currentTimeMillis()
        }
        pagamento
    }

    fun atualizar(empresaId: String, pagamento: Pagamento): Boolean = transaction {
        val linhas = Pagamentos.update({ (Pagamentos.id eq pagamento.id) and (Pagamentos.empresaId eq empresaId) }) {
            it[periodo] = pagamento.periodo
            it[valorTotal] = pagamento.valorTotal
            it[dataPagamento] = pagamento.dataPagamento
            it[status] = pagamento.status.name
            it[comprovanteUri] = pagamento.comprovanteUri
            it[lancamentoFinanceiroId] = pagamento.lancamentoFinanceiroId
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Pagamentos.update({ (Pagamentos.id eq id) and (Pagamentos.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
