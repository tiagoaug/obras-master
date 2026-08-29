package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.domain.Permissao
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única agora é FirestorePermissaoRepository (mobileMain). */
interface PermissaoRepository {
    suspend fun listarTodas(): List<Permissao>
    suspend fun listarPorColaborador(colaboradorId: String): List<Permissao>
    suspend fun definir(colaboradorId: String, moduleId: String, nivel: NivelPermissao)
    suspend fun removerTodasDoColaborador(colaboradorId: String)
    fun observarTodas(): Flow<List<Permissao>>
}
