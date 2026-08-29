package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Material
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreMaterialRepository (mobileMain). */
interface MaterialRepository {
    suspend fun listarAtivos(): List<Material>
    suspend fun salvar(material: Material)
    suspend fun atualizar(material: Material)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<Material>>
}
