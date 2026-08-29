package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.server.db.CategoriasFinanceiras
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object CategoriaFinanceiraRepository {

    private fun rowToCategoria(row: ResultRow) = CategoriaFinanceira(
        id = row[CategoriasFinanceiras.id],
        nome = row[CategoriasFinanceiras.nome],
        tipo = TipoLancamento.valueOf(row[CategoriasFinanceiras.tipo]),
        naturezaPadrao = NaturezaLancamento.valueOf(row[CategoriasFinanceiras.naturezaPadrao]),
        categoriaPaiId = row[CategoriasFinanceiras.categoriaPaiId],
        cor = row[CategoriasFinanceiras.cor],
        icone = row[CategoriasFinanceiras.icone],
        padraoDoSistema = row[CategoriasFinanceiras.padraoDoSistema],
        ativo = row[CategoriasFinanceiras.ativo],
    )

    fun listar(empresaId: String): List<CategoriaFinanceira> = transaction {
        CategoriasFinanceiras.selectAll()
            .andWhere { CategoriasFinanceiras.empresaId eq empresaId }
            .andWhere { CategoriasFinanceiras.deletedAt.isNull() }
            .map(::rowToCategoria)
    }

    fun buscarPorId(empresaId: String, id: String): CategoriaFinanceira? = transaction {
        CategoriasFinanceiras.selectAll()
            .andWhere { CategoriasFinanceiras.id eq id }
            .andWhere { CategoriasFinanceiras.empresaId eq empresaId }
            .andWhere { CategoriasFinanceiras.deletedAt.isNull() }
            .map(::rowToCategoria)
            .singleOrNull()
    }

    fun criar(empresaId: String, categoria: CategoriaFinanceira): CategoriaFinanceira = transaction {
        CategoriasFinanceiras.insert {
            it[id] = categoria.id
            it[CategoriasFinanceiras.empresaId] = empresaId
            it[nome] = categoria.nome
            it[tipo] = categoria.tipo.name
            it[naturezaPadrao] = categoria.naturezaPadrao.name
            it[categoriaPaiId] = categoria.categoriaPaiId
            it[cor] = categoria.cor
            it[icone] = categoria.icone
            it[padraoDoSistema] = categoria.padraoDoSistema
            it[ativo] = categoria.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        categoria
    }

    fun atualizar(empresaId: String, categoria: CategoriaFinanceira): Boolean = transaction {
        val linhas = CategoriasFinanceiras.update({ (CategoriasFinanceiras.id eq categoria.id) and (CategoriasFinanceiras.empresaId eq empresaId) }) {
            it[nome] = categoria.nome
            it[tipo] = categoria.tipo.name
            it[naturezaPadrao] = categoria.naturezaPadrao.name
            it[categoriaPaiId] = categoria.categoriaPaiId
            it[cor] = categoria.cor
            it[icone] = categoria.icone
            it[ativo] = categoria.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    /** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §5 — categorias padrão do sistema não são excluíveis,
     * só inativáveis. Como o "excluir" do server já é soft-delete (marca `deletedAt`, nunca some de
     * verdade), a diferença aqui é: uma categoria `padraoDoSistema` só pode ser inativada
     * (`ativo=false` via PUT), nunca passar por este endpoint de exclusão. */
    fun excluir(empresaId: String, id: String): ResultadoExclusao = transaction {
        val categoria = buscarPorIdInterno(empresaId, id) ?: return@transaction ResultadoExclusao.NAO_ENCONTRADA
        if (categoria.padraoDoSistema) return@transaction ResultadoExclusao.PADRAO_DO_SISTEMA
        val agora = System.currentTimeMillis()
        CategoriasFinanceiras.update({ (CategoriasFinanceiras.id eq id) and (CategoriasFinanceiras.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        ResultadoExclusao.EXCLUIDA
    }

    private fun buscarPorIdInterno(empresaId: String, id: String): CategoriaFinanceira? =
        CategoriasFinanceiras.selectAll()
            .andWhere { CategoriasFinanceiras.id eq id }
            .andWhere { CategoriasFinanceiras.empresaId eq empresaId }
            .andWhere { CategoriasFinanceiras.deletedAt.isNull() }
            .map(::rowToCategoria)
            .singleOrNull()

    enum class ResultadoExclusao { EXCLUIDA, NAO_ENCONTRADA, PADRAO_DO_SISTEMA }
}
