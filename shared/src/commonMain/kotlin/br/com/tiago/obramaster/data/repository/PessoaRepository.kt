package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Pessoa
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestorePessoaRepository (mobileMain). */
interface PessoaRepository {
    suspend fun listarAtivas(): List<Pessoa>
    suspend fun salvar(pessoa: Pessoa)
    suspend fun atualizar(pessoa: Pessoa)
    suspend fun desativar(id: String)
    fun observarAtivas(): Flow<List<Pessoa>>
}
