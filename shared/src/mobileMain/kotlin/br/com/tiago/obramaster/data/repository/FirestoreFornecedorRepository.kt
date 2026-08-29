package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Fornecedor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Chave é `pessoaId`, mesmo padrão de FirestoreFuncionarioRepository. */
class FirestoreFornecedorRepository(empresaContexto: EmpresaContexto) : FornecedorRepository {
    private val colecao = FirestoreCollection(empresaContexto, "fornecedores", Fornecedor.serializer())

    override suspend fun listarAtivos(): List<Fornecedor> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(fornecedor: Fornecedor) = colecao.salvar(fornecedor.pessoaId, fornecedor)
    override suspend fun atualizar(fornecedor: Fornecedor) = colecao.salvar(fornecedor.pessoaId, fornecedor)
    override suspend fun desativar(pessoaId: String) = colecao.atualizarCampos(pessoaId, "ativo" to false)
    override fun observarAtivos(): Flow<List<Fornecedor>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
