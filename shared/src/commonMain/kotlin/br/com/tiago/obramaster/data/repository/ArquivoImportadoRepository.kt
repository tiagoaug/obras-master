package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.data.db.ObraMasterDatabase
import br.com.tiago.obramaster.domain.ArquivoImportado
import br.com.tiago.obramaster.domain.FormatoImportacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ArquivoImportadoRepository {
    suspend fun listarDaPlanta(plantaId: String): List<ArquivoImportado>
    suspend fun salvar(arquivo: ArquivoImportado)
}

class SqlDelightArquivoImportadoRepository(
    private val db: ObraMasterDatabase,
) : ArquivoImportadoRepository {
    private val queries = db.arquivoImportadoQueries
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listarDaPlanta(plantaId: String): List<ArquivoImportado> = withContext(Dispatchers.Default) {
        queries.selectDaPlanta(plantaId).executeAsList().map { it.toDomain(json) }
    }

    override suspend fun salvar(arquivo: ArquivoImportado) {
        withContext(Dispatchers.Default) {
            queries.insert(
                id = arquivo.id,
                plantaId = arquivo.plantaId,
                formatoOrigem = arquivo.formatoOrigem.name,
                nomeArquivoOriginal = arquivo.nomeArquivoOriginal,
                escalaDetectadaAutomaticamente = arquivo.escalaDetectadaAutomaticamente,
                unidadeOrigem = arquivo.unidadeOrigem,
                camadasImportadasJson = json.encodeToString(arquivo.camadasImportadas),
                importadoEm = arquivo.importadoEm,
            )
        }
    }
}

private fun br.com.tiago.obramaster.db.ArquivoImportado.toDomain(json: Json) = ArquivoImportado(
    id = id,
    plantaId = plantaId,
    formatoOrigem = FormatoImportacao.valueOf(formatoOrigem),
    nomeArquivoOriginal = nomeArquivoOriginal,
    escalaDetectadaAutomaticamente = escalaDetectadaAutomaticamente,
    unidadeOrigem = unidadeOrigem,
    camadasImportadas = json.decodeFromString<List<String>>(camadasImportadasJson),
    importadoEm = importadoEm,
)
