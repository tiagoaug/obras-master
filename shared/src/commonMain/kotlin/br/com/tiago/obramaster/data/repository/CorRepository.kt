package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Cor
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreCorRepository (mobileMain). */
interface CorRepository {
    suspend fun listarAtivas(): List<Cor>
    suspend fun salvar(cor: Cor)
    suspend fun atualizar(cor: Cor)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Cor>>
}
