package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.domain.ConfigBDI
import br.com.tiago.obramaster.server.db.ConfigsBDI
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object ConfigBDIRepository {

    private fun rowToConfig(row: ResultRow) = ConfigBDI(
        id = row[ConfigsBDI.id],
        nome = row[ConfigsBDI.nome],
        administracaoCentral = row[ConfigsBDI.administracaoCentral],
        seguroGarantia = row[ConfigsBDI.seguroGarantia],
        riscos = row[ConfigsBDI.riscos],
        despesasFinanceiras = row[ConfigsBDI.despesasFinanceiras],
        lucro = row[ConfigsBDI.lucro],
        tributos = row[ConfigsBDI.tributos],
        padrao = row[ConfigsBDI.padrao],
        ativo = row[ConfigsBDI.ativo],
    )

    fun listar(empresaId: String): List<ConfigBDI> = transaction {
        ConfigsBDI.selectAll().andWhere { ConfigsBDI.empresaId eq empresaId }.andWhere { ConfigsBDI.deletedAt.isNull() }.map(::rowToConfig)
    }

    fun buscarPorId(empresaId: String, id: String): ConfigBDI? = transaction {
        ConfigsBDI.selectAll().andWhere { ConfigsBDI.id eq id }.andWhere { ConfigsBDI.empresaId eq empresaId }.andWhere { ConfigsBDI.deletedAt.isNull() }
            .map(::rowToConfig).singleOrNull()
    }

    fun criar(empresaId: String, config: ConfigBDI): ConfigBDI = transaction {
        ConfigsBDI.insert {
            it[id] = config.id
            it[ConfigsBDI.empresaId] = empresaId
            it[nome] = config.nome
            it[administracaoCentral] = config.administracaoCentral
            it[seguroGarantia] = config.seguroGarantia
            it[riscos] = config.riscos
            it[despesasFinanceiras] = config.despesasFinanceiras
            it[lucro] = config.lucro
            it[tributos] = config.tributos
            it[padrao] = config.padrao
            it[ativo] = config.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        config
    }

    fun atualizar(empresaId: String, config: ConfigBDI): Boolean = transaction {
        val linhas = ConfigsBDI.update({ (ConfigsBDI.id eq config.id) and (ConfigsBDI.empresaId eq empresaId) }) {
            it[nome] = config.nome
            it[administracaoCentral] = config.administracaoCentral
            it[seguroGarantia] = config.seguroGarantia
            it[riscos] = config.riscos
            it[despesasFinanceiras] = config.despesasFinanceiras
            it[lucro] = config.lucro
            it[tributos] = config.tributos
            it[padrao] = config.padrao
            it[ativo] = config.ativo
            it[updatedAt] = System.currentTimeMillis()
        }
        linhas > 0
    }

    fun excluir(empresaId: String, id: String): Boolean = transaction {
        val agora = System.currentTimeMillis()
        val linhas = ConfigsBDI.update({ (ConfigsBDI.id eq id) and (ConfigsBDI.empresaId eq empresaId) }) {
            it[deletedAt] = agora
            it[updatedAt] = agora
        }
        linhas > 0
    }
}
