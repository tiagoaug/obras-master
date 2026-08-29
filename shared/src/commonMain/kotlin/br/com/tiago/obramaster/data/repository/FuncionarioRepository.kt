package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.Funcionario
import kotlinx.coroutines.flow.Flow

/** Fase 10 (pivô Firebase) — implementação única é FirestoreFuncionarioRepository (mobileMain). */
interface FuncionarioRepository {
    suspend fun listarAtivos(): List<Funcionario>
    suspend fun buscarPorPessoaId(pessoaId: String): Funcionario?
    suspend fun salvar(funcionario: Funcionario)
    suspend fun atualizar(funcionario: Funcionario)
    suspend fun desativar(pessoaId: String)
    fun observarAtivos(): Flow<List<Funcionario>>
}
