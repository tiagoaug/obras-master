package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Pagamento
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestorePagamentoRepository (mobileMain). */
interface PagamentoRepository {
    suspend fun listarDaPessoa(pessoaId: String): List<Pagamento>
    suspend fun salvar(pagamento: Pagamento)
    fun observarTodos(): Flow<List<Pagamento>>
}
