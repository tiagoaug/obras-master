package br.com.tiago.obramaster.data.repository

import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreModuleConfigRepository (mobileMain). */
interface ModuleConfigRepository {
    suspend fun listarTodos(): Map<String, Boolean>
    suspend fun definir(moduleId: String, enabled: Boolean)
    fun observarTodos(): Flow<Map<String, Boolean>>
}
