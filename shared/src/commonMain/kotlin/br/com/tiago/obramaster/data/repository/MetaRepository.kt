package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.EscopoMeta
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.TipoMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface MetaRepository {
    suspend fun listarAtivas(): List<Meta>
    suspend fun salvar(meta: Meta)
    suspend fun atualizar(meta: Meta)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Meta>>
}

class SqlDelightMetaRepository(
    private val db: ObraMasterDatabase,
) : MetaRepository {
    private val queries = db.metaQueries

    override suspend fun listarAtivas(): List<Meta> = withContext(Dispatchers.Default) {
        queries.selectAtivas().executeAsList().map { it.toDomain() }
    }

    override suspend fun salvar(meta: Meta) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = meta.id,
                escopo = meta.escopo.name,
                referenciaId = meta.referenciaId,
                titulo = meta.titulo,
                tipo = meta.tipo.name,
                valorAlvo = meta.valorAlvo,
                prazo = meta.prazo,
                concluida = meta.concluida,
                ativo = meta.ativo,
            )
        }
    }

    override suspend fun atualizar(meta: Meta) {
        withContext(Dispatchers.Default) {
            queries.update(
                titulo = meta.titulo,
                valorAlvo = meta.valorAlvo,
                prazo = meta.prazo,
                concluida = meta.concluida,
                id = meta.id,
            )
        }
    }

    override suspend fun desativar(id: String) {
        withContext(Dispatchers.Default) { queries.softDelete(id) }
    }

    override fun observarAtivas(): Flow<List<Meta>> =
        queries.selectAtivas().asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.toDomain() } }
}

private fun br.com.tiago.obramaster.db.Meta.toDomain() = Meta(
    id = id,
    escopo = EscopoMeta.valueOf(escopo),
    referenciaId = referenciaId,
    titulo = titulo,
    tipo = TipoMeta.valueOf(tipo),
    valorAlvo = valorAlvo,
    prazo = prazo,
    concluida = concluida,
    ativo = ativo,
)
