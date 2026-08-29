package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Projeto
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreProjetoRepository (mobileMain). */
interface ProjetoRepository {
    suspend fun listarAtivos(): List<Projeto>
    suspend fun buscarPorId(id: String): Projeto?
    suspend fun salvar(projeto: Projeto)
    suspend fun atualizar(projeto: Projeto)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<Projeto>>
}
