package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.server.db.ModulosEmpresa
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/** Espelha ModuleRegistry.defaultState()+persisted (client, ver ModuleRegistry.kt em :shared): todo
 * AppModule começa habilitado; só guarda linha aqui quando o Gestor desativa/reativa algo. */
object ModuloRepository {

    fun listar(empresaId: String): Map<String, Boolean> = transaction {
        val overrides = ModulosEmpresa.selectAll()
            .andWhere { ModulosEmpresa.empresaId eq empresaId }
            .associate { it[ModulosEmpresa.moduleId] to it[ModulosEmpresa.enabled] }
        AppModule.entries.associate { module -> module.id to (overrides[module.id] ?: true) }
    }

    fun definir(empresaId: String, moduleId: String, enabled: Boolean) = transaction {
        val existe = ModulosEmpresa.selectAll()
            .andWhere { ModulosEmpresa.empresaId eq empresaId }
            .andWhere { ModulosEmpresa.moduleId eq moduleId }
            .any()
        if (existe) {
            ModulosEmpresa.update({ (ModulosEmpresa.empresaId eq empresaId) and (ModulosEmpresa.moduleId eq moduleId) }) {
                it[ModulosEmpresa.enabled] = enabled
            }
        } else {
            ModulosEmpresa.insert {
                it[ModulosEmpresa.empresaId] = empresaId
                it[ModulosEmpresa.moduleId] = moduleId
                it[ModulosEmpresa.enabled] = enabled
            }
        }
    }
}
