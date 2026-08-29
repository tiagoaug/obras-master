package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Projeto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreProjetoRepository(empresaContexto: EmpresaContexto) : ProjetoRepository {
    private val colecao = FirestoreCollection(empresaContexto, "projetos", Projeto.serializer())

    override suspend fun listarAtivos(): List<Projeto> = colecao.listarTodos().filter { it.ativo }
    override suspend fun buscarPorId(id: String): Projeto? = colecao.buscarPorId(id)
    override suspend fun salvar(projeto: Projeto) = colecao.salvar(projeto.id, projeto)
    override suspend fun atualizar(projeto: Projeto) = colecao.salvar(projeto.id, projeto)
    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivos(): Flow<List<Projeto>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
