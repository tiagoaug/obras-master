package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.Venda
import br.com.tiago.obramaster.server.db.ParcelasVenda
import br.com.tiago.obramaster.server.db.Vendas
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

/** Mesmo padrão de agregado de PedidoCompraRepository (Venda + suas ParcelaVenda). */
object VendaRepository {

    private fun parcelasDe(vendaId: String): List<ParcelaVenda> =
        ParcelasVenda.selectAll().andWhere { ParcelasVenda.vendaId eq vendaId }.map(::rowToParcela)

    private fun rowToParcela(row: ResultRow) = ParcelaVenda(
        id = row[ParcelasVenda.id],
        vendaId = row[ParcelasVenda.vendaId],
        numero = row[ParcelasVenda.numero],
        valor = row[ParcelasVenda.valor],
        vencimento = row[ParcelasVenda.vencimento],
        pago = row[ParcelasVenda.pago],
        lancamentoFinanceiroId = row[ParcelasVenda.lancamentoFinanceiroId],
    )

    private fun rowToVenda(row: ResultRow) = Venda(
        id = row[Vendas.id],
        projetoId = row[Vendas.projetoId],
        clientePessoaId = row[Vendas.clientePessoaId],
        descricao = row[Vendas.descricao],
        valorTotal = row[Vendas.valorTotal],
        data = row[Vendas.data],
        formaPagamento = row[Vendas.formaPagamento],
        status = StatusVenda.valueOf(row[Vendas.status]),
        ativo = row[Vendas.ativo],
    )

    private fun substituirParcelas(vendaId: String, parcelas: List<ParcelaVenda>) {
        ParcelasVenda.deleteWhere { ParcelasVenda.vendaId eq vendaId }
        if (parcelas.isNotEmpty()) {
            ParcelasVenda.batchInsert(parcelas) { parcela ->
                this[ParcelasVenda.id] = parcela.id
                this[ParcelasVenda.vendaId] = vendaId
                this[ParcelasVenda.numero] = parcela.numero
                this[ParcelasVenda.valor] = parcela.valor
                this[ParcelasVenda.vencimento] = parcela.vencimento
                this[ParcelasVenda.pago] = parcela.pago
                this[ParcelasVenda.lancamentoFinanceiroId] = parcela.lancamentoFinanceiroId
            }
        }
    }

    fun listar(empresaId: String): List<Pair<Venda, List<ParcelaVenda>>> = transaction {
        Vendas.selectAll()
            .andWhere { Vendas.empresaId eq empresaId }
            .andWhere { Vendas.deletedAt.isNull() }
            .map { row -> rowToVenda(row) to parcelasDe(row[Vendas.id]) }
    }

    fun buscarPorId(empresaId: String, id: String): Pair<Venda, List<ParcelaVenda>>? = transaction {
        Vendas.selectAll()
            .andWhere { Vendas.id eq id }
            .andWhere { Vendas.empresaId eq empresaId }
            .andWhere { Vendas.deletedAt.isNull() }
            .map { row -> rowToVenda(row) to parcelasDe(row[Vendas.id]) }
            .singleOrNull()
    }

    fun criar(empresaId: String, venda: Venda, parcelas: List<ParcelaVenda>): Pair<Venda, List<ParcelaVenda>> = transaction {
        Vendas.insert {
            it[id] = venda.id
            it[Vendas.empresaId] = empresaId
            it[projetoId] = venda.projetoId
            it[clientePessoaId] = venda.clientePessoaId
            it[descricao] = venda.descricao
            it[valorTotal] = venda.valorTotal
            it[data] = venda.data
            it[formaPagamento] = venda.formaPagamento
            it[status] = venda.status.name
            it[ativo] = venda.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        substituirParcelas(venda.id, parcelas)
        venda to parcelas
    }

    fun atualizar(empresaId: String, venda: Venda, parcelas: List<ParcelaVenda>): Boolean = transaction {
        val linhas = Vendas.update({ (Vendas.id eq venda.id) and (Vendas.empresaId eq empresaId) }) {
            it[descricao] = venda.descricao
            it[valorTotal] = venda.valorTotal
            it[data] = venda.data
            it[formaPagamento] = venda.formaPagamento
            it[status] = venda.status.name
            it[ativo] = venda.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        if (linhas > 0) substituirParcelas(venda.id, parcelas)
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Vendas.update({ (Vendas.id eq id) and (Vendas.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
