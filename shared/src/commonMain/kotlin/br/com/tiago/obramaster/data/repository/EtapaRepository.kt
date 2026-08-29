package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Etapa
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreEtapaRepository (mobileMain). */
interface EtapaRepository {
    suspend fun listarDoProjeto(projetoId: String): List<Etapa>
    suspend fun listarTodasAtivas(): List<Etapa>
    suspend fun salvar(etapa: Etapa)
    suspend fun atualizar(etapa: Etapa)
    suspend fun reordenar(etapaId: String, novaOrdem: Int)
    suspend fun desativar(id: String)
    fun observarDoProjeto(projetoId: String): Flow<List<Etapa>>
}
