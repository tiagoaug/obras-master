package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Fornecedor
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreFornecedorRepository (mobileMain). */
interface FornecedorRepository {
    suspend fun listarAtivos(): List<Fornecedor>
    suspend fun salvar(fornecedor: Fornecedor)
    suspend fun atualizar(fornecedor: Fornecedor)
    suspend fun desativar(pessoaId: String)
    fun observarAtivos(): Flow<List<Fornecedor>>
}
