package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.assistant.ManualIndex
import br.com.tiago.obramaster.core.assistant.ManualSection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import obramaster.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

interface ManualRepository {
    suspend fun listarSecoes(): List<ManualSection>
    suspend fun buscarPorId(id: String): ManualSection?
}

/** SPEC_ASSISTENTE_IA.md §2.1 — `manual_index.json` é gerado (hoje, mantido à mão) a partir de
 * `docs/MANUAL_DO_PROGRAMA.md` e empacotado como asset no app; carregado uma vez e cacheado em
 * memória, igual ao SeedNormaABNTRepository (mesmo padrão de dado de referência somente leitura). */
class SeedManualRepository : ManualRepository {
    private val mutex = Mutex()
    private var cache: List<ManualSection>? = null
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun carregar(): List<ManualSection> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: run {
                val bytes = Res.readBytes("files/manual_index.json")
                val indice = json.decodeFromString<ManualIndex>(bytes.decodeToString())
                cache = indice.secoes
                indice.secoes
            }
        }
    }

    override suspend fun listarSecoes(): List<ManualSection> = carregar()

    override suspend fun buscarPorId(id: String): ManualSection? = carregar().find { it.id == id }
}
