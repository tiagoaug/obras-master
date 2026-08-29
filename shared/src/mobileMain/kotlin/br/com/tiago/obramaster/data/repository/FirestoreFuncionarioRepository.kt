package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Funcionario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Chave é `pessoaId` (não tem `id` próprio) — Funcionario é extensão 1:1 de Pessoa, mesmo padrão do :server. */
class FirestoreFuncionarioRepository(empresaContexto: EmpresaContexto) : FuncionarioRepository {
    private val colecao = FirestoreCollection(empresaContexto, "funcionarios", Funcionario.serializer())

    override suspend fun listarAtivos(): List<Funcionario> = colecao.listarTodos().filter { it.ativo }
    override suspend fun buscarPorPessoaId(pessoaId: String): Funcionario? = colecao.buscarPorId(pessoaId)
    override suspend fun salvar(funcionario: Funcionario) = colecao.salvar(funcionario.pessoaId, funcionario)
    override suspend fun atualizar(funcionario: Funcionario) = colecao.salvar(funcionario.pessoaId, funcionario)
    override suspend fun desativar(pessoaId: String) = colecao.atualizarCampos(pessoaId, "ativo" to false)
    override fun observarAtivos(): Flow<List<Funcionario>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
