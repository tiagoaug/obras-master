package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — a única implementação agora é FirestoreColaboradorRepository
 * (mobileMain); a senha não faz mais parte deste repositório (Firebase Auth cuida disso, ver
 * FirebaseAuthGateway). */
interface ColaboradorRepository {
    suspend fun listarAtivos(): List<Colaborador>
    suspend fun buscarPorId(id: String): Colaborador?
    suspend fun buscarPorLogin(login: String): Colaborador?
    suspend fun atualizar(colaborador: Colaborador)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<Colaborador>>
}
