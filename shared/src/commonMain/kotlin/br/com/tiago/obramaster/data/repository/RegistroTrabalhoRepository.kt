package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.RegistroTrabalho
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreRegistroTrabalhoRepository (mobileMain). */
interface RegistroTrabalhoRepository {
    suspend fun listarDaPessoa(pessoaId: String): List<RegistroTrabalho>
    suspend fun listarNaoPagosDaPessoa(pessoaId: String): List<RegistroTrabalho>
    suspend fun listarTodos(): List<RegistroTrabalho>
    suspend fun salvar(registro: RegistroTrabalho)
    suspend fun marcarPagos(ids: List<String>, pagamentoId: String)
    suspend fun desativar(id: String)
    fun observarTodos(): Flow<List<RegistroTrabalho>>
}
