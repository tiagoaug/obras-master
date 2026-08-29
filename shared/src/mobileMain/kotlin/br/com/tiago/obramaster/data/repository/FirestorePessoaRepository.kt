package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Pessoa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestorePessoaRepository(empresaContexto: EmpresaContexto) : PessoaRepository {
    private val colecao = FirestoreCollection(empresaContexto, "pessoas", Pessoa.serializer())

    override suspend fun listarAtivas(): List<Pessoa> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(pessoa: Pessoa) = colecao.salvar(pessoa.id, pessoa)
    override suspend fun atualizar(pessoa: Pessoa) = colecao.salvar(pessoa.id, pessoa)
    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivas(): Flow<List<Pessoa>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
