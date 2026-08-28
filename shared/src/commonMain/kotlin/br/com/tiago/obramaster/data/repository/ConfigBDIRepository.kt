package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.ConfigBDI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ConfigBDIRepository {
    suspend fun listarAtivos(): List<ConfigBDI>

    /** Se [config].padrao for true, desmarca padrao dos demais — só um perfil padrão por vez. */
    suspend fun salvar(config: ConfigBDI)
    suspend fun atualizar(config: ConfigBDI)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<ConfigBDI>>
}

class SqlDelightConfigBDIRepository(
    private val db: ObraMasterDatabase,
) : ConfigBDIRepository {
    private val queries = db.configBDIQueries

    override suspend fun listarAtivos(): List<ConfigBDI> = withContext(Dispatchers.Default) {
        queries.selectAtivos().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(config: ConfigBDI) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insert(
                    id = config.id,
                    nome = config.nome,
                    administracaoCentral = config.administracaoCentral,
                    seguroGarantia = config.seguroGarantia,
                    riscos = config.riscos,
                    despesasFinanceiras = config.despesasFinanceiras,
                    lucro = config.lucro,
                    tributos = config.tributos,
                    padrao = config.padrao,
                    ativo = config.ativo,
                )
                if (config.padrao) queries.limparPadrao(config.id)
            }
        }
    }

    override suspend fun atualizar(config: ConfigBDI) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.update(
                    nome = config.nome,
                    administracaoCentral = config.administracaoCentral,
                    seguroGarantia = config.seguroGarantia,
                    riscos = config.riscos,
                    despesasFinanceiras = config.despesasFinanceiras,
                    lucro = config.lucro,
                    tributos = config.tributos,
                    padrao = config.padrao,
                    id = config.id,
                )
                if (config.padrao) queries.limparPadrao(config.id)
            }
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivos(): Flow<List<ConfigBDI>> =
        queries.selectAtivos().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.ConfigBDI.toDomain() = ConfigBDI(
    id = id,
    nome = nome,
    administracaoCentral = administracaoCentral,
    seguroGarantia = seguroGarantia,
    riscos = riscos,
    despesasFinanceiras = despesasFinanceiras,
    lucro = lucro,
    tributos = tributos,
    padrao = padrao,
    ativo = ativo,
)
