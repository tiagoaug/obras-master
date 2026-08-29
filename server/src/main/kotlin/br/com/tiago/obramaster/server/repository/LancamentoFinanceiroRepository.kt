package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.server.db.LancamentosFinanceiros
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/** GUIA_ANTIGRAVITY.md §4 regra 9 / SPEC_OBRA_MASTER_KMP.md §6.3 — "registros financeiros são
 * imutáveis após criados; correção é por estorno". Por isso este repositório só tem `criar` e
 * leitura: nenhum `atualizar`/`excluir`, de propósito — não é omissão. */
object LancamentoFinanceiroRepository {

    private fun rowToLancamento(row: ResultRow) = LancamentoFinanceiro(
        id = row[LancamentosFinanceiros.id],
        tipo = TipoLancamento.valueOf(row[LancamentosFinanceiros.tipo]),
        categoriaId = row[LancamentosFinanceiros.categoriaId],
        centroDeCustoId = row[LancamentosFinanceiros.centroDeCustoId],
        natureza = NaturezaLancamento.valueOf(row[LancamentosFinanceiros.natureza]),
        projetoId = row[LancamentosFinanceiros.projetoId],
        etapaId = row[LancamentosFinanceiros.etapaId],
        descricao = row[LancamentosFinanceiros.descricao],
        valor = row[LancamentosFinanceiros.valor],
        data = row[LancamentosFinanceiros.data],
        formaPagamento = row[LancamentosFinanceiros.formaPagamento],
        pago = row[LancamentosFinanceiros.pago],
        pessoaId = row[LancamentosFinanceiros.pessoaId],
        anexoUri = row[LancamentosFinanceiros.anexoUri],
        contaId = row[LancamentosFinanceiros.contaId],
        ativo = row[LancamentosFinanceiros.ativo],
    )

    fun listar(empresaId: String, projetoId: String? = null): List<LancamentoFinanceiro> = transaction {
        LancamentosFinanceiros.selectAll()
            .andWhere { LancamentosFinanceiros.empresaId eq empresaId }
            .let { query -> if (projetoId != null) query.andWhere { LancamentosFinanceiros.projetoId eq projetoId } else query }
            .map(::rowToLancamento)
    }

    fun buscarPorId(empresaId: String, id: String): LancamentoFinanceiro? = transaction {
        LancamentosFinanceiros.selectAll()
            .andWhere { LancamentosFinanceiros.id eq id }
            .andWhere { LancamentosFinanceiros.empresaId eq empresaId }
            .map(::rowToLancamento)
            .singleOrNull()
    }

    fun criar(empresaId: String, lancamento: LancamentoFinanceiro): LancamentoFinanceiro = transaction {
        LancamentosFinanceiros.insert {
            it[id] = lancamento.id
            it[LancamentosFinanceiros.empresaId] = empresaId
            it[tipo] = lancamento.tipo.name
            it[categoriaId] = lancamento.categoriaId
            it[centroDeCustoId] = lancamento.centroDeCustoId
            it[natureza] = lancamento.natureza.name
            it[projetoId] = lancamento.projetoId
            it[etapaId] = lancamento.etapaId
            it[descricao] = lancamento.descricao
            it[valor] = lancamento.valor
            it[data] = lancamento.data
            it[formaPagamento] = lancamento.formaPagamento
            it[pago] = lancamento.pago
            it[pessoaId] = lancamento.pessoaId
            it[anexoUri] = lancamento.anexoUri
            it[contaId] = lancamento.contaId
            it[ativo] = lancamento.ativo
            it[criadoEm] = System.currentTimeMillis()
        }
        lancamento
    }
}
