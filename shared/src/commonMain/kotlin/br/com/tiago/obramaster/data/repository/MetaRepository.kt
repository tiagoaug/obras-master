package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Meta
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreMetaRepository (mobileMain). */
interface MetaRepository {
    suspend fun listarAtivas(): List<Meta>
    suspend fun salvar(meta: Meta)
    suspend fun atualizar(meta: Meta)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Meta>>
}
