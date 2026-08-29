package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.CentroDeCusto
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreCentroDeCustoRepository (mobileMain). */
interface CentroDeCustoRepository {
    suspend fun listarAtivos(): List<CentroDeCusto>
    suspend fun buscarPorProjetoId(projetoId: String): CentroDeCusto?
    suspend fun salvar(centro: CentroDeCusto)
    suspend fun atualizar(centro: CentroDeCusto)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<CentroDeCusto>>
}
