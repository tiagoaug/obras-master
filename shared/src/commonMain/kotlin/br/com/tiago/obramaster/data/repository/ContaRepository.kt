package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Conta
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreContaRepository (mobileMain). */
interface ContaRepository {
    suspend fun listarAtivas(): List<Conta>
    suspend fun buscarPorId(id: String): Conta?
    suspend fun salvar(conta: Conta)

    /** saldoInicial/dataSaldoInicial não são editáveis aqui — usar um MovimentoConta tipo AJUSTE pra corrigir saldo depois de criada. */
    suspend fun atualizar(conta: Conta)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Conta>>
}
