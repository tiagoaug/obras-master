package br.com.tiago.obramaster.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.CategoriaNorma
import br.com.tiago.obramaster.domain.DocumentoTecnico
import br.com.tiago.obramaster.domain.TipoDocumento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface DocumentoTecnicoRepository {
    suspend fun listarTodos(): List<DocumentoTecnico>
    suspend fun salvar(documento: DocumentoTecnico)
    suspend fun excluir(id: String)
    fun observarTodos(): Flow<List<DocumentoTecnico>>

    /** SPEC_AREA_EXECUTOR.md §4 — chamado depois que o PdfTextExtractor termina, em background. */
    suspend fun atualizarTextoExtraido(id: String, texto: String)

    /** SPEC_AREA_EXECUTOR.md §3 — busca full-text sobre o texto extraído dos PDFs (índice FTS
     * no SQLDelight; substring simples no repositório em memória da Web). */
    suspend fun buscarPorTexto(query: String): List<DocumentoTecnico>
}

class SqlDelightDocumentoTecnicoRepository(
    private val db: ObraMasterDatabase,
) : DocumentoTecnicoRepository {
    private val queries = db.documentoTecnicoQueries
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarTodos(): List<DocumentoTecnico> = withContext(Dispatchers.Default) {
        val tagsPorDocumento = queries.selectTodasTags().executeAsList().groupBy({ it.documentoId }, { it.tag })
        queries.selectTodos().executeAsList().map { it.toDomain(tagsPorDocumento[it.id].orEmpty(), json) }
    }

    override suspend fun salvar(documento: DocumentoTecnico) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.insert(
                    id = documento.id,
                    nome = documento.nome,
                    tipo = documento.tipo.name,
                    categoria = documento.categoria.name,
                    arquivoKey = documento.arquivoKey,
                    tamanhoBytes = documento.tamanhoBytes,
                    normaVinculadaId = documento.normaVinculadaId,
                    vinculadaEtapasTemplateJson = json.encodeToString(documento.vinculadaEtapasTemplate),
                    vinculadaMaterialId = documento.vinculadaMaterialId,
                    textoExtraido = documento.textoExtraido,
                    adicionadoEm = documento.adicionadoEm,
                )
                documento.tags.forEach { queries.insertTag(documento.id, it) }
            }
        }
    }

    override suspend fun excluir(id: String) {
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.deleteFtsDe(id)
                queries.deleteTagsDe(id)
                queries.delete(id)
            }
        }
    }

    override suspend fun atualizarTextoExtraido(id: String, texto: String) {
        if (texto.isBlank()) return
        withContext(Dispatchers.Default) {
            db.transaction {
                queries.updateTextoExtraido(texto, id)
                queries.deleteFtsDe(id)
                queries.insertFts(id, texto)
            }
        }
    }

    override suspend fun buscarPorTexto(query: String): List<DocumentoTecnico> {
        if (query.isBlank()) return listarTodos()
        val idsEncontrados: Set<String> = withContext(Dispatchers.Default) {
            queries.buscarPorTexto(query).executeAsList().mapNotNull { it.documentoId }.toSet()
        }
        return listarTodos().filter { it.id in idsEncontrados }
    }

    override fun observarTodos(): Flow<List<DocumentoTecnico>> =
        combine(
            queries.selectTodos().asFlow().mapToList(Dispatchers.Default),
            queries.selectTodasTags().asFlow().mapToList(Dispatchers.Default),
        ) { documentos, tags ->
            val tagsPorDocumento = tags.groupBy({ it.documentoId }, { it.tag })
            documentos.map { it.toDomain(tagsPorDocumento[it.id].orEmpty(), json) }
        }
}

private fun br.com.tiago.obramaster.db.DocumentoTecnico.toDomain(tags: List<String>, json: Json) = DocumentoTecnico(
    id = id,
    nome = nome,
    tipo = runCatching { TipoDocumento.valueOf(tipo) }.getOrDefault(TipoDocumento.OUTRO),
    categoria = runCatching { CategoriaNorma.valueOf(categoria) }.getOrDefault(CategoriaNorma.OUTRA),
    arquivoKey = arquivoKey,
    tamanhoBytes = tamanhoBytes,
    normaVinculadaId = normaVinculadaId,
    tags = tags,
    vinculadaEtapasTemplate = runCatching { json.decodeFromString<List<String>>(vinculadaEtapasTemplateJson) }.getOrDefault(emptyList()),
    vinculadaMaterialId = vinculadaMaterialId,
    textoExtraido = textoExtraido,
    adicionadoEm = adicionadoEm,
)
