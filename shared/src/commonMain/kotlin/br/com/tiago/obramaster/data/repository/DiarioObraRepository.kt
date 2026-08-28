package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.DiarioObra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface DiarioObraRepository {
    suspend fun listarDoProjeto(projetoId: String): List<DiarioObra>

    /** [diario].fotosUris substitui a lista inteira de fotos do registro (mesmo padrão de Equipe.membrosIds). */
    suspend fun salvar(diario: DiarioObra)
    suspend fun atualizar(diario: DiarioObra)
    suspend fun desativar(id: String)
    fun observarDoProjeto(projetoId: String): Flow<List<DiarioObra>>
}

class SqlDelightDiarioObraRepository(
    private val db: ObraMasterDatabase,
) : DiarioObraRepository {
    private val queries = db.diarioObraQueries

    override suspend fun listarDoProjeto(projetoId: String): List<DiarioObra> = withContext(Dispatchers.Default) {
        val fotosPorRegistro = queries.selectTodasFotos().executeAsList().groupBy({ it.diarioObraId }, { it.fotoKey })
        queries.selectAtivosDoProjeto(projetoId).executeAsList().map { it.toDomain(fotosPorRegistro[it.id].orEmpty()) }
    }

    override suspend fun salvar(diario: DiarioObra) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insert(
                    id = diario.id,
                    projetoId = diario.projetoId,
                    etapaId = diario.etapaId,
                    data_ = diario.data,
                    texto = diario.texto,
                    clima = diario.clima,
                    ativo = diario.ativo,
                )
                diario.fotosUris.forEachIndexed { indice, chave -> queries.insertFoto(diario.id, chave, indice.toLong()) }
            }
        }
    }

    override suspend fun atualizar(diario: DiarioObra) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.update(
                    etapaId = diario.etapaId,
                    data_ = diario.data,
                    texto = diario.texto,
                    clima = diario.clima,
                    id = diario.id,
                )
                queries.deleteFotosDoRegistro(diario.id)
                diario.fotosUris.forEachIndexed { indice, chave -> queries.insertFoto(diario.id, chave, indice.toLong()) }
            }
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarDoProjeto(projetoId: String): Flow<List<DiarioObra>> =
        combine(
            queries.selectAtivosDoProjeto(projetoId).asFlow().mapToList(Dispatchers.Default),
            queries.selectTodasFotos().asFlow().mapToList(Dispatchers.Default),
        ) { registros, fotos ->
            val fotosPorRegistro = fotos.groupBy({ it.diarioObraId }, { it.fotoKey })
            registros.map { it.toDomain(fotosPorRegistro[it.id].orEmpty()) }
        }
}

private fun br.com.tiago.obramaster.db.DiarioObra.toDomain(fotosUris: List<String>) = DiarioObra(
    id = id,
    projetoId = projetoId,
    etapaId = etapaId,
    data = data_,
    texto = texto,
    clima = clima,
    fotosUris = fotosUris,
    ativo = ativo,
)
