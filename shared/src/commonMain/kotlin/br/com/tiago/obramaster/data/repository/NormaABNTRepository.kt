package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.NormaABNT
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import obramaster.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

interface NormaABNTRepository {
    suspend fun listarTodas(): List<NormaABNT>
}

/** SPEC_AREA_EXECUTOR.md §2.1 — o catálogo é distribuído como seed dentro do app (JSON em
 * Compose Resources), não editável pelo usuário comum nesta fase. Carregado uma vez e
 * cacheado em memória — não precisa de tabela no banco, é dado de referência somente leitura. */
class SeedNormaABNTRepository : NormaABNTRepository {
    private val mutex = Mutex()
    private var cache: List<NormaABNT>? = null
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun listarTodas(): List<NormaABNT> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: run {
                val bytes = Res.readBytes("files/normas_abnt_seed.json")
                val normas = json.decodeFromString<List<NormaABNT>>(bytes.decodeToString())
                cache = normas
                normas
            }
        }
    }
}
