package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Equipe
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreEquipeRepository (mobileMain). */
interface EquipeRepository {
    suspend fun listarAtivas(): List<Equipe>

    /** [membrosIds] substitui a lista inteira de membros da equipe. */
    suspend fun salvar(equipe: Equipe)
    suspend fun atualizar(equipe: Equipe)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Equipe>>
}
