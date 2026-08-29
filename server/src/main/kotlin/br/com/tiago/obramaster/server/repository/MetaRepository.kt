package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.EscopoMeta
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.TipoMeta
import br.com.tiago.obramaster.server.db.Metas
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object MetaRepository {

    private fun rowToMeta(row: ResultRow) = Meta(
        id = row[Metas.id],
        escopo = EscopoMeta.valueOf(row[Metas.escopo]),
        referenciaId = row[Metas.referenciaId],
        titulo = row[Metas.titulo],
        tipo = TipoMeta.valueOf(row[Metas.tipo]),
        valorAlvo = row[Metas.valorAlvo],
        prazo = row[Metas.prazo],
        concluida = row[Metas.concluida],
        ativo = row[Metas.ativo],
    )

    fun listar(empresaId: String): List<Meta> = transaction {
        Metas.selectAll().andWhere { Metas.empresaId eq empresaId }.andWhere { Metas.deletedAt.isNull() }.map(::rowToMeta)
    }

    fun buscarPorId(empresaId: String, id: String): Meta? = transaction {
        Metas.selectAll().andWhere { Metas.id eq id }.andWhere { Metas.empresaId eq empresaId }.andWhere { Metas.deletedAt.isNull() }
            .map(::rowToMeta).singleOrNull()
    }

    fun criar(empresaId: String, meta: Meta): Meta = transaction {
        Metas.insert {
            it[id] = meta.id
            it[Metas.empresaId] = empresaId
            it[escopo] = meta.escopo.name
            it[referenciaId] = meta.referenciaId
            it[titulo] = meta.titulo
            it[tipo] = meta.tipo.name
            it[valorAlvo] = meta.valorAlvo
            it[prazo] = meta.prazo
            it[concluida] = meta.concluida
            it[ativo] = meta.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        meta
    }

    fun atualizar(empresaId: String, meta: Meta): Boolean = transaction {
        val linhas = Metas.update({ (Metas.id eq meta.id) and (Metas.empresaId eq empresaId) }) {
            it[titulo] = meta.titulo
            it[valorAlvo] = meta.valorAlvo
            it[prazo] = meta.prazo
            it[concluida] = meta.concluida
            it[ativo] = meta.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = Metas.update({ (Metas.id eq id) and (Metas.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
