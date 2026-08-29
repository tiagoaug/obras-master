package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.StatusOrcamento
import br.com.tiago.obramaster.domain.TipoItemOrcamento
import br.com.tiago.obramaster.server.db.ItensOrcamento
import br.com.tiago.obramaster.server.db.Orcamentos
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

/** Mesmo padrão de agregado de PedidoCompraRepository (Orcamento + seus ItemOrcamento). */
object OrcamentoRepository {

    private fun itensDe(orcamentoId: String): List<ItemOrcamento> =
        ItensOrcamento.selectAll().andWhere { ItensOrcamento.orcamentoId eq orcamentoId }.map(::rowToItem)

    private fun rowToItem(row: ResultRow) = ItemOrcamento(
        id = row[ItensOrcamento.id],
        orcamentoId = row[ItensOrcamento.orcamentoId],
        tipo = TipoItemOrcamento.valueOf(row[ItensOrcamento.tipo]),
        descricao = row[ItensOrcamento.descricao],
        materialId = row[ItensOrcamento.materialId],
        quantidade = row[ItensOrcamento.quantidade],
        unidade = row[ItensOrcamento.unidade],
        valorUnitario = row[ItensOrcamento.valorUnitario],
        valorTotal = row[ItensOrcamento.valorTotal],
    )

    private fun rowToOrcamento(row: ResultRow) = Orcamento(
        id = row[Orcamentos.id],
        projetoId = row[Orcamentos.projetoId],
        clientePessoaId = row[Orcamentos.clientePessoaId],
        titulo = row[Orcamentos.titulo],
        data = row[Orcamentos.data],
        validadeDias = row[Orcamentos.validadeDias],
        status = StatusOrcamento.valueOf(row[Orcamentos.status]),
        descontoPercent = row[Orcamentos.descontoPercent],
        observacoes = row[Orcamentos.observacoes],
        configBdiId = row[Orcamentos.configBdiId],
        bdiPercentualCalculado = row[Orcamentos.bdiPercentualCalculado],
        bdiCustomizado = row[Orcamentos.bdiCustomizado],
        custoDiretoTotal = row[Orcamentos.custoDiretoTotal],
        precoVendaTotal = row[Orcamentos.precoVendaTotal],
        ativo = row[Orcamentos.ativo],
    )

    private fun substituirItens(orcamentoId: String, itens: List<ItemOrcamento>) {
        ItensOrcamento.deleteWhere { ItensOrcamento.orcamentoId eq orcamentoId }
        if (itens.isNotEmpty()) {
            ItensOrcamento.batchInsert(itens) { item ->
                this[ItensOrcamento.id] = item.id
                this[ItensOrcamento.orcamentoId] = orcamentoId
                this[ItensOrcamento.tipo] = item.tipo.name
                this[ItensOrcamento.descricao] = item.descricao
                this[ItensOrcamento.materialId] = item.materialId
                this[ItensOrcamento.quantidade] = item.quantidade
                this[ItensOrcamento.unidade] = item.unidade
                this[ItensOrcamento.valorUnitario] = item.valorUnitario
                this[ItensOrcamento.valorTotal] = item.valorTotal
            }
        }
    }

    fun listar(empresaId: String): List<Pair<Orcamento, List<ItemOrcamento>>> = transaction {
        Orcamentos.selectAll()
            .andWhere { Orcamentos.empresaId eq empresaId }
            .andWhere { Orcamentos.deletedAt.isNull() }
            .map { row -> rowToOrcamento(row) to itensDe(row[Orcamentos.id]) }
    }

    fun buscarPorId(empresaId: String, id: String): Pair<Orcamento, List<ItemOrcamento>>? = transaction {
        Orcamentos.selectAll()
            .andWhere { Orcamentos.id eq id }
            .andWhere { Orcamentos.empresaId eq empresaId }
            .andWhere { Orcamentos.deletedAt.isNull() }
            .map { row -> rowToOrcamento(row) to itensDe(row[Orcamentos.id]) }
            .singleOrNull()
    }

    fun criar(empresaId: String, orcamento: Orcamento, itens: List<ItemOrcamento>): Pair<Orcamento, List<ItemOrcamento>> = transaction {
        Orcamentos.insert {
            it[id] = orcamento.id
            it[Orcamentos.empresaId] = empresaId
            it[projetoId] = orcamento.projetoId
            it[clientePessoaId] = orcamento.clientePessoaId
            it[titulo] = orcamento.titulo
            it[data] = orcamento.data
            it[validadeDias] = orcamento.validadeDias
            it[status] = orcamento.status.name
            it[descontoPercent] = orcamento.descontoPercent
            it[observacoes] = orcamento.observacoes
            it[configBdiId] = orcamento.configBdiId
            it[bdiPercentualCalculado] = orcamento.bdiPercentualCalculado
            it[bdiCustomizado] = orcamento.bdiCustomizado
            it[custoDiretoTotal] = orcamento.custoDiretoTotal
            it[precoVendaTotal] = orcamento.precoVendaTotal
            it[ativo] = orcamento.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        substituirItens(orcamento.id, itens)
        orcamento to itens
    }

    fun atualizar(empresaId: String, orcamento: Orcamento, itens: List<ItemOrcamento>): Boolean = transaction {
        val linhas = Orcamentos.update({ (Orcamentos.id eq orcamento.id) and (Orcamentos.empresaId eq empresaId) }) {
            it[projetoId] = orcamento.projetoId
            it[clientePessoaId] = orcamento.clientePessoaId
            it[titulo] = orcamento.titulo
            it[data] = orcamento.data
            it[validadeDias] = orcamento.validadeDias
            it[status] = orcamento.status.name
            it[descontoPercent] = orcamento.descontoPercent
            it[observacoes] = orcamento.observacoes
            it[configBdiId] = orcamento.configBdiId
            it[bdiPercentualCalculado] = orcamento.bdiPercentualCalculado
            it[bdiCustomizado] = orcamento.bdiCustomizado
            it[custoDiretoTotal] = orcamento.custoDiretoTotal
            it[precoVendaTotal] = orcamento.precoVendaTotal
            it[ativo] = orcamento.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        if (linhas > 0) substituirItens(orcamento.id, itens)
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Orcamentos.update({ (Orcamentos.id eq id) and (Orcamentos.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
