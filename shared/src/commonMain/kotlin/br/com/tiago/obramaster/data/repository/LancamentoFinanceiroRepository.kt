package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreLancamentoFinanceiroRepository (mobileMain). */
interface LancamentoFinanceiroRepository {
    suspend fun listarAtivos(): List<LancamentoFinanceiro>
    suspend fun salvar(lancamento: LancamentoFinanceiro)
    suspend fun atualizar(lancamento: LancamentoFinanceiro)
    suspend fun marcarPago(id: String, pago: Boolean)
    suspend fun desativar(id: String)
    fun observarAtivos(): Flow<List<LancamentoFinanceiro>>
}
