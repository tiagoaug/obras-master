package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.PlantaBaixa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PlantaBaixaRepository {
    suspend fun listarDoProjeto(projetoId: String): List<PlantaBaixa>
    suspend fun buscarPorId(id: String): PlantaBaixa?
    suspend fun salvar(planta: PlantaBaixa)
    suspend fun atualizarEscala(id: String, escalaPxPorMetro: Double, atualizadaEm: Long)
    suspend fun atualizarImagemFundo(id: String, imagemFundoKey: String?, atualizadaEm: Long)
    suspend fun atualizarOpacidadeFundo(id: String, opacidade: Float)
    suspend fun renomear(id: String, nome: String, atualizadaEm: Long)
    suspend fun desativar(id: String)
    fun observarDoProjeto(projetoId: String): Flow<List<PlantaBaixa>>
}

class SqlDelightPlantaBaixaRepository(
    private val db: ObraMasterDatabase,
) : PlantaBaixaRepository {
    private val queries = db.plantaBaixaQueries

    override suspend fun listarDoProjeto(projetoId: String): List<PlantaBaixa> = withContext(Dispatchers.Default) {
        queries.selectAtivasDoProjeto(projetoId).executeAsList().map { it.toDomain() }
    }

    override suspend fun buscarPorId(id: String): PlantaBaixa? = withContext(Dispatchers.Default) {
        queries.selectPorId(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun salvar(planta: PlantaBaixa) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = planta.id,
                projetoId = planta.projetoId,
                nome = planta.nome,
                escalaPxPorMetro = planta.escalaPxPorMetro,
                imagemFundoKey = planta.imagemFundoKey,
                imagemFundoOpacidade = planta.imagemFundoOpacidade.toDouble(),
                criadaEm = planta.criadaEm,
                atualizadaEm = planta.atualizadaEm,
                ativo = planta.ativo,
            )
        }
    }

    override suspend fun atualizarEscala(id: String, escalaPxPorMetro: Double, atualizadaEm: Long) {
        withContext(Dispatchers.Default) { queries.updateEscala(escalaPxPorMetro, atualizadaEm, id) }
    }

    override suspend fun atualizarImagemFundo(id: String, imagemFundoKey: String?, atualizadaEm: Long) {
        withContext(Dispatchers.Default) { queries.atualizarImagemFundo(imagemFundoKey, atualizadaEm, id) }
    }

    override suspend fun atualizarOpacidadeFundo(id: String, opacidade: Float) {
        withContext(Dispatchers.Default) { queries.atualizarOpacidadeFundo(opacidade.toDouble(), id) }
    }

    override suspend fun renomear(id: String, nome: String, atualizadaEm: Long) {
        withContext(Dispatchers.Default) { queries.renomear(nome, atualizadaEm, id) }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarDoProjeto(projetoId: String): Flow<List<PlantaBaixa>> =
        queries.selectAtivasDoProjeto(projetoId).asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.PlantaBaixa.toDomain() = PlantaBaixa(
    id = id,
    projetoId = projetoId,
    nome = nome,
    escalaPxPorMetro = escalaPxPorMetro,
    imagemFundoKey = imagemFundoKey,
    imagemFundoOpacidade = imagemFundoOpacidade.toFloat(),
    criadaEm = criadaEm,
    atualizadaEm = atualizadaEm,
    ativo = ativo,
)
