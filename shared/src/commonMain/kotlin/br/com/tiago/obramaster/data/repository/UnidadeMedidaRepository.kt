package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.UnidadeMedida
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreUnidadeMedidaRepository (mobileMain). */
interface UnidadeMedidaRepository {
    suspend fun listarAtivas(): List<UnidadeMedida>
    suspend fun salvar(unidade: UnidadeMedida)
    suspend fun atualizar(unidade: UnidadeMedida)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<UnidadeMedida>>
}
